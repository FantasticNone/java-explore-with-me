package ru.practicum.stats.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.stats.server.exception.BadRequestException;
import ru.practicum.stats.server.service.HitService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final HitService hitService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public HitDto createHit(@Valid @RequestBody HitDto hitDto) {
        log.info("Register request (app = {}, uri = {}, ip = {}, timestamp = {})",
                hitDto.getApp(),
                hitDto.getUri(),
                hitDto.getIp(),
                hitDto.getTimestamp());
        return hitService.createHit(hitDto);
    }

    @GetMapping("/stats")
    public List<StatsDto> getStatistic(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(defaultValue = "false") Boolean unique) {
        if (start == null || end == null) {
            throw new BadRequestException("Start date and end date are required for the request");
        }
        log.info("Get statistic (from = {}, to = {}, uris = {}, unique = {})",
                start,
                end,
                uris,
                unique);
        return hitService.getStatistic(start, end, uris, unique);
    }

}
