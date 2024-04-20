package ru.practicum.ewm.service.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.mapper.*;
import ru.practicum.ewm.model.compilation.Compilation;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventsRepository eventsRepository;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        List<Event> eventsForCompilation = eventsRepository.findAllByIdIn(newCompilationDto.getEvents());
        Compilation compilation = compilationRepository.save(CompilationMapper.toCompilation(newCompilationDto));

        return CompilationMapper.toCompilationDto(compilation, eventsForCompilation.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList())
        );
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
        Pageable pageable = PageRequest.of(from / size, size);
        List<CompilationDto> compilationDtos = new ArrayList<>();
        List<Compilation> compilations;

        if (pinned != null)
            compilations = compilationRepository.findAllByPinnedEquals(pinned, pageable);
        else
            compilations = compilationRepository.findAll(pageable).getContent();

        for (Compilation compilation : compilations) {
            List<EventShortDto> events = eventsRepository.findAllByIdIn(compilation.getEvents()).stream()
                    .map(EventMapper::toEventShortDto)
                    .collect(Collectors.toList());
            compilationDtos.add(CompilationMapper.toCompilationDto(compilation, events));
        }

        return compilationDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilation(long compId) {
        Compilation fromDb = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found with id: " + compId));
        List<EventShortDto> events = getEventsByIds(fromDb.getEvents());
        return CompilationMapper.toCompilationDto(fromDb, events);
    }


    private List<EventShortDto> getEventsByIds(List<Long> ids) {
        return eventsRepository.findEventsByIds(ids).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compId)));

        List<Event> events = eventsRepository.findAllByIdIn(updateCompilationRequest.getEvents());
        compilation.setEvents(events.stream()
                .map(Event::getId)
                .collect(Collectors.toList())
        );
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        compilation.setPinned(updateCompilationRequest.getPinned());

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation), events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList())
        );
    }
}


