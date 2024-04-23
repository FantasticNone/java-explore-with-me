package ru.practicum.ewm.service.event;

import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.event.search.AdminSearchEventsParams;
import ru.practicum.ewm.dto.event.search.PublicSearchEventsParams;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.model.event.Event;

import java.util.List;
import java.util.Map;

public interface EventService {

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto getEvent(long userId, long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventRequest newEventDto);

    List<RequestDto> getEventRequests(Long userId, Long eventId);

    List<EventFullDto> getEventsByAdmin(AdminSearchEventsParams params);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventRequest updateDto);

    List<EventShortDto> getEventsByPublic(PublicSearchEventsParams params);

    EventFullDto getEventByPublic(Long eventId);

    Long getEventViews(Event event);

    Map<Long, Long> getEventsViews(List<Event> events);

}
