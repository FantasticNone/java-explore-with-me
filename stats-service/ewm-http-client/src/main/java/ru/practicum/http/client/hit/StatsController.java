package ru.practicum.http.client.hit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.HitDto;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Validated
@Slf4j
public class StatsController {

    private final StatsClient statsClient;

    @PostMapping("/hit")
    public ResponseEntity<Object> createHit(@RequestBody HitDto hitDto) {
        log.debug("Gateway: creating hit");
        return statsClient.createHit(hitDto);
    }

    @GetMapping("/stats")
    public ResponseEntity<Object> getStatistic(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                           @RequestParam(required = false) List<String> uris,
                                           @RequestParam(required = false) Boolean unique) {

        log.debug("Gateway: getting stats");
        return statsClient.getStatistic(start, end, uris, unique);
    }
}