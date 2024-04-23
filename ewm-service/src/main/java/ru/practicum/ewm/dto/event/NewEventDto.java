package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.dto.location.LocationDto;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class NewEventDto {

    @NotBlank
    @Size(min = 20, max = 2000, message = "Annotation limit")
    private String annotation;

    @NotNull
    private Long category;

    @NotBlank
    @Size(min = 20, max = 7000, message = "Description limit")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    @Future(message = "Date can not be in past")
    private LocalDateTime eventDate;

    private Integer confirmedRequests;
    private LocationDto location;
    private Boolean paid = false;

    @PositiveOrZero
    private int participantLimit = 0;
    private Boolean requestModeration = true;

    @NotBlank
    @Size(min = 3, max = 120, message = "Title limit")
    private String title;
}
