package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.model.Compilation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {

    /*public Compilation toCompilation(NewCompilationDto newCompilationDto, List<Event> events) {
       return Compilation.builder()
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.getPinned())
                .events(events)
                .build();
    }*/

    public CompilationDto toCompilationDto(Compilation compilation, Map<Long, Long> views) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(EventMapper.toListShortDto(compilation.getEvents(), views))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public static List<CompilationDto> toListDto(List<Compilation> compilationList, Map<Long, Map<Long, Long>> views) {
        return compilationList.stream()
                .map(c -> CompilationMapper.toCompilationDto(c, views.get(c.getId())))
                .collect(Collectors.toList());
    }
}
