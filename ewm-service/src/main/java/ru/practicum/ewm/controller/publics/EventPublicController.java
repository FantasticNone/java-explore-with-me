package ru.practicum.ewm.controller.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.PublicSearchEventsParams;
import ru.practicum.ewm.service.event.EventService;
import ru.practicum.http.client.hit.StatsClient;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@Slf4j
@RequiredArgsConstructor
public class EventPublicController {
    private final EventService eventService;
    private final StatsClient statsClient;

    @GetMapping
    public List<EventShortDto> getEventsBySearch(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Min(1)int size,
            HttpServletRequest httpServletRequest) {
        log.info("Public: search events (text = {}, categories = {}, paid = {}, rangeStart = {}, rangeEnd = {}, " +
                        "onlyAvailable = {}, sort = {}, from = {}, size = {})",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        PublicSearchEventsParams params = PublicSearchEventsParams.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .ip(httpServletRequest.getRemoteAddr())
                .uri(httpServletRequest.getRequestURI())
                .build();

        return eventService.getEventsByPublic(params);
    }

    @GetMapping("/{id}")
    public EventDto getEvent(@PathVariable(value = "id") Long eventId, HttpServletRequest httpServletRequest) {
        log.info("Get event by public (event id = {})", eventId);

        statsClient.createHit("ewm-main-service", httpServletRequest);

        return eventService.getEventByPublic(eventId);
    }
}