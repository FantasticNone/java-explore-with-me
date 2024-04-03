package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.stats.server.exception.IncorrectDataException;
import ru.practicum.stats.server.mapper.EndpointHitMapper;
import ru.practicum.stats.server.model.EndpointHit;
import ru.practicum.stats.server.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.stats.server.mapper.EndpointHitMapper.*;


@Service
@RequiredArgsConstructor
public class HitServiceImpl implements HitService {

    private final HitRepository hitRepository;

    @Override
    public HitDto createHit(HitDto hitDto) {

        hitRepository.save(toEndpointHit(hitDto));
        return hitDto;
    }

    @Override
    public List<StatsDto> getStatistic(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        List<EndpointHit> endpointHits;
        List<StatsDto> stats;

        if (start.isAfter(end))
            throw new IncorrectDataException("Range end is before Range start");

        if (uris == null || uris.isEmpty()) {
            endpointHits = hitRepository.findAllHitsBetweenDates(start, end);
        } else {
            endpointHits = hitRepository.findByTimestampAndUris(start, end, uris);
        }

        stats = EndpointHitMapper.toResponse(endpointHits, unique);

        return stats;
    }
}
