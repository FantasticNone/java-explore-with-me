package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class HitDto {

    @NotBlank(message = "URI must not be blank")
    private String uri;

    @NotBlank(message = "IP must not be blank")
    @Size(min = 7, message = "IP is too short")
    private String ip;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @PastOrPresent(message = "Timestamp can't be in future")
    @NotNull
    private LocalDateTime timestamp;

    @NotBlank(message = "App must not be blank")
    private String app;
}