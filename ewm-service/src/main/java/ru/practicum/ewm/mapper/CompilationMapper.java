package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.event.Event;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {

    public Compilation toCompilation(NewCompilationDto newCompilationDto, List<Event> events) {
       return Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.getPinned())
                .events(events)
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation, Map<Long, Long> views) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(EventMapper.toListShortDto(compilation.getEvents(), views))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    /*public static List<CompilationDto> toDto(List<Compilation> compilationList, Map<Long, Map<Long, Long>> views) {
        return compilationList.stream()
                .map(c -> CompilationMapper.toDto(c, views.get(c.getId())))
                .collect(Collectors.toList());
    }*/
}
