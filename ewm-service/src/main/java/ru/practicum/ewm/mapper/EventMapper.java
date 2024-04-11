package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.dto.event.EventDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class EventMapper {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static Event toEventFromNewEvent(NewEventDto event, User user, Category category, LocalDateTime createdOn) {
        return Event.builder()
                .initiator(user)
                .state(EventState.PENDING)
                .annotation(event.getAnnotation())
                .eventDate(event.getEventDate())
                .confirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0)
                .paid(event.getPaid())
                .requestModeration(event.getRequestModeration())
                .participantLimit(event.getParticipantLimit())
                .location(event.getLocation())
                .description(event.getDescription())
                .createdOn(createdOn)
                .category(category)
                .title(event.getTitle())
                .build();
    }
    public EventDto toEventDtoFromEvent(Event event) {
        UserDto user = UserMapper.toUserDto(event.getInitiator());
        return EventDto.builder()
                .id(event.getId())
                .eventDate(event.getEventDate().format(DTF))
                .category(event.getCategory())
                .initiator(user)
                .annotation(event.getAnnotation())
                .confirmedRequests(Long.valueOf(event.getConfirmedRequests()))
                .createdOn(event.getCreatedOn().format(DTF))
                .description(event.getDescription())
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .state(event.getState())
                .publishedOn(event.getPublishedOn() != null ? event.getPublishedOn().format(DTF) : null)
                .title(event.getTitle())
                .requestModeration(event.getRequestModeration())
                //.views(Long.valueOf(event.getViews() != null ? event.getViews() : 0))
                .build();
    }

    public static EventDto toDtoWithViews(Event event, Long views) {
        UserDto user = UserMapper.toUserDto(event.getInitiator());
        return EventDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(event.getCategory())
                .confirmedRequests(event.getRequests().stream().filter(request -> request.getStatus().equals(Request.Status.CONFIRMED)).count())
                .createdOn(event.getCreatedOn().format(DTF))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(DTF))
                .initiator(user)
                .location(EventLocationMapper.toLocation(event.getEventLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn().format(DTF))
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public static EventShortDto toEventShortDto(Event event, Long views) {
        UserDto user = UserMapper.toUserDto(event.getInitiator());
        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .paid(event.getPaid())
                .views(Long.valueOf(views))
                .confirmedRequests(event.getRequests().stream().filter(request -> request.getStatus().equals(Request.Status.CONFIRMED)).count())
                .annotation(event.getAnnotation())
                .category(event.getCategory())
                .eventDate(event.getEventDate())
                .initiator(user)
                .build();
    }

    public static List<EventShortDto> toListShortDto(List<Event> events, Map<Long, Long> views) {
        return events.stream()
                .map(event -> EventMapper.toEventShortDto(event, views.get(event.getId())))
                .collect(Collectors.toList());
    }

    public static List<EventShortDto> toCountListShortDto(List<Event> events, Long views) {
        return events.stream()
                .map(event -> EventMapper.toEventShortDto(event, views))
                .collect(Collectors.toList());
    }

}
