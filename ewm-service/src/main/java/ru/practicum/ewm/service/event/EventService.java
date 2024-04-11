package ru.practicum.ewm.service.event;

import ru.practicum.ewm.dto.event.EventDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.model.event.Event;

import java.util.List;
import java.util.Map;

public interface EventService {

    EventDto createEvent(long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(long userId, int from, int size);

    EventDto getEvent(long userId, long eventId);

    Long getEventViews(Event event);

    Map<Long, Long> getEventsViews(List<Event> events);

}
