package ru.practicum.ewm.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.StatsDto;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.exceptions.DataValidationFailException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.repository.CategoriesRepository;
import ru.practicum.ewm.repository.EventsRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.http.client.hit.StatsClient;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventsRepository eventsRepository;
    private final UserRepository userRepository;
    private final CategoriesRepository categoryRepository;

    private final StatsClient statsClient;

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
        return EventMapper.toCountListShortDto(userEvents, eventCount);
        //return EventMapper.toListShortDto(userEvents, getEventsViews(userEvents));
    }

    @Override
    public EventDto getEvent(long userId, long eventId) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));

        Event event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

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
                return (long) eventRequests.get(0).getHits();
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
                    .collect(Collectors.toMap(r -> Long.valueOf(r.getUri().substring(8)), r -> (long) r.getHits()));
        } else {
            return Collections.emptyMap();
        }
    }
}