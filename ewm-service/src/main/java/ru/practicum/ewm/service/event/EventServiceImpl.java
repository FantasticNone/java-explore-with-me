package ru.practicum.ewm.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.exceptions.BadRequestException;
import ru.practicum.ewm.exceptions.ConditionsViolationException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.exceptions.DataValidationFailException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.EventLocation;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.repository.CategoriesRepository;
import ru.practicum.ewm.repository.EventLocationRepository;
import ru.practicum.ewm.repository.EventsRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.http.client.hit.StatsClient;
import org.springframework.beans.factory.annotation.Value;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import javax.persistence.criteria.Predicate;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.HOURS;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventsRepository eventsRepository;
    private final UserRepository userRepository;
    private final CategoriesRepository categoryRepository;
    private final EventLocationRepository eventLocationRepository;

    private final StatsClient statsClient;
    @Value("${app}")
    String app;

    @Override
    @Transactional
    public EventDto createEvent(long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DataValidationFailException("Should be date that has not yet occurred");
        }

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", newEventDto.getCategory())));
        Event event = EventMapper.toEventFromNewEvent(newEventDto, initiator, category, LocalDateTime.now());
        eventsRepository.save(event);
        return EventMapper.toEventDtoFromEvent(event);
    }

    @Override
    public List<EventShortDto> getUserEvents(long userId, int from, int size) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        List<Event> userEvents = eventsRepository.getUserEvents(userId, PageRequest.of(from / size, size));
        long eventCount = eventsRepository.countByInitiatorId(userId);
        //return EventMapper.toCountListShortDto(userEvents, eventCount);
        return EventMapper.toListShortDto(userEvents, getEventsViews(userEvents));
    }

    @Override
    public EventDto getEvent(long userId, long eventId) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));

        Event event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        return EventMapper.toDtoWithViews(event, getEventViews(event));
    }

    @Override
    @Transactional
    public EventDto updateEvent(long userId, long eventId, UpdateEventUserRequest newEventDto) {
        if (newEventDto.getEventDate() != null) {
            if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new DataValidationFailException("Should be date that has not yet occurred");
            }
        }


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        Event event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        if (!event.getInitiator().getId().equals(user.getId()))
            throw new ConditionsViolationException("Only event initiator can update this event");
        if (!event.getState().equals(EventState.PENDING) && !event.getState().equals(EventState.CANCELED))
            throw new ConditionsViolationException("Only pending or canceled events can be changed");

        if (newEventDto.getAnnotation() != null)
            event.setAnnotation(newEventDto.getAnnotation());

        if (newEventDto.getCategory() != null) {
            Category category = categoryRepository.findById(newEventDto.getCategory().getId())
                    .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", newEventDto.getCategory().getId())));
            event.setCategory(category);
        }

        if (newEventDto.getDescription() != null)
            event.setDescription(newEventDto.getDescription());
        if (newEventDto.getEventDate() != null)
            event.setEventDate(newEventDto.getEventDate());

        if (newEventDto.getLocation() != null) {
            if (!eventLocationRepository.existsByLatAndLon(newEventDto.getLocation().getLat(), newEventDto.getLocation().getLon())) {
                EventLocation eventLocation = EventLocation.builder()
                        .lat(newEventDto.getLocation().getLat())
                        .lon(newEventDto.getLocation().getLon())
                        .event(event)
                        .build();
                EventLocation newEventLocation = eventLocationRepository.save(eventLocation);

                event.setEventLocation(newEventLocation);
            }
        }

        if (newEventDto.getPaid() != null)
            event.setPaid(newEventDto.getPaid());
        if (newEventDto.getParticipantLimit() != null)
            event.setParticipantLimit(newEventDto.getParticipantLimit());
        if (newEventDto.getRequestModeration() != null)
            event.setRequestModeration(newEventDto.getRequestModeration());


        String stateString = newEventDto.getStateAction();
        if (stateString != null && !stateString.isBlank()) {
            switch (StateActionUser.valueOf(stateString)) {
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
            }
        }

        if (newEventDto.getTitle() != null)
            event.setTitle(newEventDto.getTitle());

        Event newEvent = eventsRepository.save(event);
        return EventMapper.toDtoWithViews(newEvent, getEventViews(newEvent));
    }

    @Override
    public List<RequestDto> getEventRequests(long userId, long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        Event event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        List<Request> eventRequests = event.getRequests();

        return RequestMapper.toListRequestDto(eventRequests);
    }

    @Override
    public List<EventDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        List<Event> events;
        List<EventState> searchingStates;

        Pageable pageable = PageRequest.of(from / size, size);

        if (states != null) {
            searchingStates = Arrays.stream(EventState.values())
                    .filter(e -> states.contains(e.toString()))
                    .collect(Collectors.toList());
        } else {
            searchingStates = Arrays.stream(EventState.values())
                    .collect(Collectors.toList());
        }

        if (users != null && categories != null) {
            if (rangeStart != null && rangeEnd != null) {
                events = eventsRepository.findByUsersAndCategoriesWithTimestamp(users, categories, searchingStates, rangeStart, rangeEnd, pageable);
            } else {
                events = eventsRepository.findByUsersAndCategories(users, categories, searchingStates, pageable);
            }
        } else if (users != null) {
            if (rangeStart != null && rangeEnd != null) {
                events = eventsRepository.findByUsersWithTimestamp(users, searchingStates, rangeStart, rangeEnd, pageable);
            } else {
                events = eventsRepository.findByUsers(users, searchingStates, pageable);
            }
        } else if (categories != null) {
            if (rangeStart != null && rangeEnd != null) {
                events = eventsRepository.findByCategoriesWithTimestamp(categories, searchingStates, rangeStart, rangeEnd, pageable);
            } else {
                events = eventsRepository.findByCategories(categories, searchingStates, pageable);
            }
        } else {
            if (rangeStart != null && rangeEnd != null) {
                events = eventsRepository.findWithTimestamp(searchingStates, rangeStart, rangeEnd, pageable);
            } else {
                events = eventsRepository.findByStates(searchingStates, pageable);
            }
        }

        return EventMapper.toListDto(events, getEventsViews(events));
    }

    @Override
    @Transactional
    public EventDto updateEventAdmin(long eventId, UpdateEventAdminRequest updateDto) {
        Event event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        String stateString = updateDto.getStateAction();
        if (stateString != null && !stateString.isBlank()) {
            switch (StateActionAdmin.valueOf(stateString)) {
                case PUBLISH_EVENT:
                    if (HOURS.between(LocalDateTime.now(), event.getEventDate()) < 1) {
                        throw new DataValidationFailException("Начало события должно быть минимум на один час позже момента публикации");
                    }
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConditionsViolationException("Попытка опубликовать событие с id=" + event.getId() + ", которое уже опубликовано.");
                    }
                    if (event.getState() == EventState.CANCELED) {
                        throw new ConditionsViolationException("Попытка опубликовать событие с id=" + event.getId() + ", которое уже отменено.");
                    }
                    event.setState(EventState.PUBLISHED);
                    break;
                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConditionsViolationException("Попытка отменить событие с id=" + event.getId() + ", которое уже опубликовано.");
                    }
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        if (updateDto.getAnnotation() != null)
            event.setAnnotation(updateDto.getAnnotation());
        if (updateDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateDto.getCategory())
                    .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", updateDto.getCategory())));

            event.setCategory(category);
        }
        if (updateDto.getDescription() != null)
            event.setDescription(updateDto.getDescription());

        if (updateDto.getEventDate() != null) {
            if (updateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new DataValidationFailException("Should be date that has not yet occurred");
            }

                event.setEventDate(updateDto.getEventDate());
            }
            if (updateDto.getLocation() != null) {
                EventLocation eventLocation = eventLocationRepository.findByLatAndLon(updateDto.getLocation().getLat(), updateDto.getLocation().getLon());
                if (eventLocation == null) {
                    EventLocation newEventLocation = EventLocation.builder()
                            .lat(updateDto.getLocation().getLat())
                            .lon(updateDto.getLocation().getLon())
                            .build();

                    newEventLocation = eventLocationRepository.save(newEventLocation);

                    event.setEventLocation(newEventLocation);
                } else {
                    event.setEventLocation(eventLocation);
                }
            }
            if (updateDto.getPaid() != null)
                event.setPaid(updateDto.getPaid());
            if (updateDto.getParticipantLimit() != null)
                event.setParticipantLimit(updateDto.getParticipantLimit());
            if (updateDto.getRequestModeration() != null)
                event.setRequestModeration(updateDto.getRequestModeration());
            if (updateDto.getStateAction() != null) {
                if (updateDto.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT)) {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                } else {
                    event.setState(EventState.CANCELED);
                }
            }
            if (updateDto.getTitle() != null)
                event.setTitle(updateDto.getTitle());

        Event newEvent = eventsRepository.save(event);
        return EventMapper.toDtoWithViews(newEvent, getEventViews(newEvent));
    }

    @Override
    public List<EventShortDto> getEventsByPublic(PublicSearchEventsParams params) {
        LocalDateTime start = params.getRangeStart();
        LocalDateTime end = params.getRangeEnd();

        if (start == null) params.setRangeStart(LocalDateTime.now());

        if (end != null && Objects.requireNonNull(start).isAfter(end))
            throw new DataValidationFailException("Start time can't be after End time");

        Specification<Event> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> row = new ArrayList<>();

            if (params.getCategories() != null) {
                row.add(root.get("category").get("id").in(params.getCategories()));
            }

            if (params.getPaid() != null) {
                row.add(criteriaBuilder.equal(root.get("paid"), params.getPaid()));
            }

            if (params.getOnlyAvailable() != null && params.getOnlyAvailable()) {
                row.add(criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("participantLimit"), 0),
                        criteriaBuilder.lessThan(root.get("participantLimit"), root.get("confirmedRequest"))
                ));
            }
            LocalDateTime current = LocalDateTime.now();
            LocalDateTime startDateTime = Objects.requireNonNullElse(params.getRangeStart(), current);
            row.add(criteriaBuilder.greaterThan(root.get("eventDate"), startDateTime));
            if (params.getRangeEnd() != null) {
                row.add(criteriaBuilder.lessThan(root.get("eventDate"), params.getRangeEnd()));
            }
            if (params.getText() != null && !params.getText().isBlank()) {
                String likeText = "%" + params.getText() + "%";
                row.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("annotation"), likeText),
                        criteriaBuilder.like(root.get("description"), likeText)
                ));
            }
            return criteriaBuilder.and(row.toArray(new Predicate[0]));
        };

        int from = params.getFrom(), size = params.getSize();
        Pageable pageable = PageRequest.of(from / size, size);
        if (params.getSort() != null) {
            switch (params.getSort()) {
                case "EVENT_DATE":
                    pageable = PageRequest.of(from / size, size, Sort.Direction.ASC, "eventDate");
                    break;
                case "VIEWS":
                    pageable = PageRequest.of(from / size, size, Sort.Direction.DESC, "views");
                    break;
            }
        }
        List<Event> events = eventsRepository.findAll(spec, pageable);
        if (events.isEmpty()) return List.of();

        return EventMapper.toListShortDto(events, getEventsViews(events));
    }

    @Override
    public EventDto getEventByPublic(Long eventId) {
        Event event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        if (!event.getState().equals(EventState.PUBLISHED))
            throw new NotFoundException("Event must be published");

            eventsRepository.save(event);

        return EventMapper.toDtoWithViews(event, getEventViews(event));

    }

    public Long getEventViews(Event event) {
        if (event.getId() != null) {
            List<StatsDto> eventRequests = statsClient.getStatistics(
                    LocalDateTime.now().minusYears(100),
                    LocalDateTime.now().plusYears(100),
                    List.of("/events/" + event.getId()),
                    true);

            if (!eventRequests.isEmpty())
                return eventRequests.get(0).getHits();
        }

        return 0L;
    }

    public Map<Long, Long> getEventsViews(List<Event> events) {
        List<String> eventsUri = events.stream()
                .map(e -> "/events/" + e.getId())
                .collect(Collectors.toList());

        List<StatsDto> eventsRequests = statsClient.getStatistics(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100),
                eventsUri,
                true);

        if (!events.isEmpty()) {
            return eventsRequests.stream()
                    .collect(Collectors.toMap(r -> Long.valueOf(r.getUri().substring(8)), r -> r.getHits()));
        } else {
            return Collections.emptyMap();
        }
    }
}