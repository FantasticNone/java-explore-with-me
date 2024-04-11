package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.model.event.Location;
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
    @Size(max = 2000, message = "Annotation too long")
    @Size(min = 20, message = "Annotation too short")
    private String annotation;
    @NotNull
    private Long category;
    @NotBlank
    @Size(max = 7000, message = "Description too long")
    @Size(min = 20, message = "Description too short")
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull
    @Future(message = "Date can not be in past")
    private LocalDateTime eventDate;
    private Integer confirmedRequests;
    @NotNull
    private Location location;
    private Boolean paid = false;
    @PositiveOrZero
    private Long participantLimit = 0L;
    private Boolean requestModeration = true;
    @NotBlank
    @Size(max = 120, message = "Title too long")
    @Size(min = 3, message = "Title too short")
    private String title;
}
