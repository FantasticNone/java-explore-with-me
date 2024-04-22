package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.model.compilation.Compilation;

import java.util.List;

@UtilityClass
public class CompilationMapper {
    public Compilation toCompilation(NewCompilationDto newCompilationDto) {
        return Compilation.builder()
                .pinned(newCompilationDto.isPinned())
                .title(newCompilationDto.getTitle())
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> eventsShortDto) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(eventsShortDto)
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }
}

