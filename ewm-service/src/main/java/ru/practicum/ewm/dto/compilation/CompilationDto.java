package ru.practicum.ewm.dto.compilation;

import lombok.*;
import ru.practicum.ewm.dto.event.EventShortDto;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class CompilationDto {

    private Long id;
    private List<EventShortDto> events;
    private Boolean pinned;
    private String title;
}