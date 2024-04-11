package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;

public interface CompilationService {

    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    /*void deleteCompilation(Long compId);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, NewCompilationDto updateCompilationDto);*/
}