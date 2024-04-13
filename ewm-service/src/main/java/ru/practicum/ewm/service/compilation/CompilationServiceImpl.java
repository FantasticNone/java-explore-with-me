package ru.practicum.ewm.service.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventsRepository;
import ru.practicum.ewm.service.event.EventService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventsRepository eventsRepository;
    private final EventService eventsService;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        List<Event> events = Collections.emptyList();

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty())
            events = eventsRepository.findAllById(newCompilationDto.getEvents());

        Compilation compilation = Compilation.builder()
                .events(events)
                .pinned(newCompilationDto.getPinned())
                .title(newCompilationDto.getTitle())
                .build();

        Compilation newCompilation = compilationRepository.saveAndFlush(compilation);
        return CompilationMapper.toCompilationDto(newCompilation, getEventsViewsByCompilation(newCompilation));

    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compId)));

        compilationRepository.delete(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        List<Boolean> pinnedFilter;

        if (pinned != null)
            pinnedFilter = List.of(pinned);
        else
            pinnedFilter = List.of(Boolean.TRUE, Boolean.FALSE);

        List<Compilation> compilations = compilationRepository.findAllWherePinned(pinnedFilter, PageRequest.of(from / size, size));

        return CompilationMapper.toListDto(compilations, mapEventsViewsByCompilations(compilations));
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compId)));

        return CompilationMapper.toCompilationDto(compilation, getEventsViewsByCompilation(compilation));
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, NewCompilationDto updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compId)));

        List<Event> events = null;
        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty())
            events = eventsRepository.findAllById(updateCompilationRequest.getEvents());

        if (events != null)
            compilation.setEvents(events);
        if (updateCompilationRequest.getPinned() != null)
            compilation.setPinned(updateCompilationRequest.getPinned());
        if (updateCompilationRequest.getTitle() != null)
            compilation.setTitle(updateCompilationRequest.getTitle());


        Compilation newCompilation = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(newCompilation, getEventsViewsByCompilation(newCompilation));
    }

    public Map<Long, Long> getEventsViewsByCompilation(Compilation compilation) {
        return eventsService.getEventsViews(compilation.getEvents());
    }

    public Map<Long, Map<Long, Long>> mapEventsViewsByCompilations(List<Compilation> compilations) {
        return compilations.stream()
                .collect(Collectors.toMap(Compilation::getId, this::getEventsViewsByCompilation));
    }
}


