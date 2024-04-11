package ru.practicum.ewm.dto.event;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.event.Location;


import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class EventDto {

    private long id;
    private String annotation;
    private String title;
    private Long confirmedRequests;
    private String createdOn;
    private String eventDate;
    private String publishedOn;
    private String description;
    private UserDto initiator;
    private Category category;
    private Location location;
    private Boolean paid;
    private Boolean requestModeration;
    private Long participantLimit;
    private EventState state;
    private Long views;
}