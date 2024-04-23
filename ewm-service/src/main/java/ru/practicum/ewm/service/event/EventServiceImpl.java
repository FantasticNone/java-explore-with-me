package ru.practicum.ewm.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.dto.StatsDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.event.search.AdminSearchEventsParams;
import ru.practicum.ewm.dto.event.search.PublicSearchEventsParams;
import ru.practicum.ewm.dto.event.state.StateActionAdmin;
import ru.practicum.ewm.dto.event.state.StateActionUser;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.exceptions.ConditionsViolationException;
import ru.practicum.ewm.exceptions.ConflictDataException;
import ru.practicum.ewm.mapper.*;
import ru.practicum.ewm.exceptions.DataValidationFailException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.event.Location;
import ru.practicum.ewm.model.request.Request;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.repository.*;
import ru.practicum.http.client.hit.StatsClient;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import javax.persistence.criteria.Predicate;
import java.util.stream.Collectors;
import java.util.function.BiConsumer;

import static ru.practicum.ewm.dto.event.state.StateActionAdmin.*;
import static ru.practicum.ewm.model.event.EventState.*;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventsRepository eventsRepository;
    private final UserRepository userRepository;
    private final CategoriesRepository categoryRepository;
    private final LocationRepository locationRepository;

    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DataValidationFailException("Should be date that has not yet occurred");
        }
        User initiator = checkUserId(userId);
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", newEventDto.getCategory())));
        Location location = locationRepository.save(LocationMapper.toLocation(newEventDto.getLocation()));

        Event event = EventMapper.toEvent(newEventDto);
        event.setCategory(category);
        event.setInitiator(initiator);
        event.setLocation(location);
        event.setState(PENDING);
        event.setCreatedOn(LocalDateTime.now());
        return EventMapper.toEventFullDto(eventsRepository.save(event));
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        checkUserId(userId);
        List<Event> userEvents = eventsRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size));
        return userEvents.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEvent(long userId, long eventId) {
        checkUserId(userId);
        Event event = checkEventId(eventId);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventRequest newEventDto) {
        if (newEventDto.getEventDate() != null) {
            if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new DataValidationFailException("Should be date that has not yet occurred");
            }
        }

        User user = checkUserId(userId);
        Event event = checkEventId(eventId);

        if (!event.getInitiator().getId().equals(user.getId()))
            throw new ConditionsViolationException("Only event initiator can update this event");
        if (!event.getState().equals(PENDING) && !event.getState().equals(EventState.CANCELED))
            throw new ConditionsViolationException("Only pending or canceled events can be changed");

        if (newEventDto.getCategory() != null) {
            Category category = categoryRepository.findById(newEventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", newEventDto.getCategory())));
            event.setCategory(category);
        }

        if (newEventDto.getLocation() != null) {
            if (!locationRepository.existsByLatAndLon(newEventDto.getLocation().getLat(), newEventDto.getLocation().getLon())) {
                Location eventLocation = Location.builder()
                        .lat(newEventDto.getLocation().getLat())
                        .lon(newEventDto.getLocation().getLon())
                        .build();
                Location newEventLocation = locationRepository.save(eventLocation);

                event.setLocation(newEventLocation);
            }
        }

        String stateString = newEventDto.getStateAction();
        if (stateString != null && !stateString.isBlank()) {
            switch (StateActionUser.valueOf(stateString)) {
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(PENDING);
                    break;
            }
        }

        updateEventFields(event, newEventDto);

        Event newEvent = eventsRepository.save(event);
        return EventMapper.toEventFullDto(newEvent);
    }

    @Override
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        checkUserId(userId);
        Event event = checkEventId(eventId);

        List<Request> eventRequests = event.getRequests();

        return RequestMapper.toListRequestDto(eventRequests);
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(AdminSearchEventsParams params) {
        LocalDateTime start = params.getRangeStart();
        LocalDateTime end = params.getRangeEnd();

        if (start == null) params.setRangeStart(LocalDateTime.now());

        if (end != null && Objects.requireNonNull(start).isAfter(end))
            throw new ConflictDataException("Start time can't be after End time");

        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Specification<Event> spec = Specification.where(null);

        spec = spec.and((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), params.getRangeStart()));

        if (end != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), end));
        }
        if (params.getUsers() != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get("initiator").get("id").in(params.getUsers()));
        }

        if (params.getCategories() != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get("category").in(params.getCategories()));
        }

        return eventsRepository.findAll(spec, pageable).stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventRequest updateDto) {
        Event event = checkEventId(eventId);

        if (updateDto.getCategory() != null) {
            long catId = updateDto.getCategory();
            Category category = categoryRepository.findById(catId)
                    .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", catId)
                    ));
            event.setCategory(category);
        }
        if (updateDto.getEventDate() != null) {
            LocalDateTime actualEventTime = event.getEventDate();
            if (actualEventTime.plusHours(1).isAfter(updateDto.getEventDate()) ||
                    actualEventTime.plusHours(1) != updateDto.getEventDate())
                event.setEventDate(updateDto.getEventDate());

            else
                throw new ConflictDataException("eventDate can't be earlier than one hour from the date of publication.");
        }

        if (updateDto.getLocation() != null) {
            event.setLocation(locationRepository.save(LocationMapper.toLocation(updateDto.getLocation())));
        }

        if (updateDto.getStateAction() != null) {
            if (event.getState() == PENDING) {
                StateActionAdmin action = StateActionAdmin.toEnum(updateDto.getStateAction());
                if (action == PUBLISH_EVENT) {
                    event.setState(PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                } else if (action == REJECT_EVENT) {
                    event.setState(CANCELED);
                }
            } else {
                throw new ConflictDataException(String.format(
                        "Cannot publish the event because it's not in the right state: %S",
                        event.getState())
                );
            }
        }
        updateEventFields(event, updateDto);
        return EventMapper.toEventFullDto(eventsRepository.save(event));
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
    public EventFullDto getEventByPublic(Long eventId) {
        Event event = checkEventId(eventId);

        if (!event.getState().equals(EventState.PUBLISHED))
            throw new NotFoundException("Event must be published");

        eventsRepository.save(event);

        EventFullDto eventByPublic = EventMapper.toEventFullDto(event);

        eventByPublic.setViews(getEventViews(event));

        return eventByPublic;

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

    private User checkUserId(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        return user;
    }

    private Event checkEventId(long eventId) {
        return eventsRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
    }

    private void updateEventFields(Event savedEvent, UpdateEventRequest newEvent) {
        Map<String, BiConsumer<Event, UpdateEventRequest>> fieldsUpdaters = new HashMap<>();
        fieldsUpdaters.put("annotation",
                (event, eventForUpdate) -> event.setAnnotation(eventForUpdate.getAnnotation()));
        fieldsUpdaters.put("description",
                (event, eventForUpdate) -> event.setDescription(eventForUpdate.getDescription()));
        fieldsUpdaters.put("paid",
                ((event, eventForUpdate) -> event.setPaid(eventForUpdate.getPaid())));
        fieldsUpdaters.put("participantLimit",
                ((event, eventForUpdate) -> event.setParticipantLimit(eventForUpdate.getParticipantLimit())));
        fieldsUpdaters.put("requestModeration",
                ((event, eventForUpdate) -> event.setRequestModeration(eventForUpdate.getRequestModeration())));
        fieldsUpdaters.put("title",
                ((event, eventForUpdate) -> event.setTitle(eventForUpdate.getTitle())));

        fieldsUpdaters.forEach((field, updater) -> {
            switch (field) {
                case "annotation":
                    if (newEvent.getAnnotation() != null) updater.accept(savedEvent, newEvent);
                    break;
                case "description":
                    if (newEvent.getDescription() != null) updater.accept(savedEvent, newEvent);
                    break;
                case "paid":
                    if (newEvent.getPaid() != null) updater.accept(savedEvent, newEvent);
                    break;
                case "participantLimit":
                    if (newEvent.getParticipantLimit() != null) updater.accept(savedEvent, newEvent);
                    break;
                case "requestModeration":
                    if (newEvent.getRequestModeration() != null) updater.accept(savedEvent, newEvent);
                    break;
                case "title":
                    if (newEvent.getTitle() != null) updater.accept(savedEvent, newEvent);
                    break;
            }
        });
    }
}