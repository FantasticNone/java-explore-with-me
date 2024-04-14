package ru.practicum.ewm.service.event;

import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.model.event.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventService {

    EventDto createEvent(long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(long userId, int from, int size);

    EventDto getEvent(long userId, long eventId);

    EventDto updateEvent(long userId, long eventId, UpdateEventUserRequest newEventDto);

    List<RequestDto> getEventRequests(long userId, long eventId);

    List<EventDto> getEventsByAdmin(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventDto updateEventAdmin(long eventId, UpdateEventAdminRequest updateDto);

    List<EventShortDto> getEventsByPublic(PublicSearchEventsParams params);

    EventDto getEventByPublic(Long eventId);

    Long getEventViews(Event event);

    Map<Long, Long> getEventsViews(List<Event> events);

}
