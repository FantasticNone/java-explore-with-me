package ru.practicum.ewm.dto.location;

import lombok.*;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class EventLocationDto {
    private Float lat;
    private Float lon;
}