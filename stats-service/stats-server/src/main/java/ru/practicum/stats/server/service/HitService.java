package ru.practicum.stats.server.service;

import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface HitService {

    HitDto createHit(HitDto hitDto);

    List<StatsDto> getStatistic(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);

}