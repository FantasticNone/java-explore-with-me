package ru.practicum.http.client.hit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Slf4j
public class StatsClient {

    private final String statsServerUrl;
    private final RestTemplate restTemplate;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats.server.url}") String statsServerUrl) {
        this.statsServerUrl = statsServerUrl;
        restTemplate = new RestTemplate();
    }

    public void createHit(String app, HttpServletRequest request) {
        HitDto hitDto = HitDto.builder()
                .app(app)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        log.info("Send request data to stats-service (app = {}, uri = {}, ip = {})",
                hitDto.getApp(),
                hitDto.getUri(),
                hitDto.getIp(),

                restTemplate.postForObject(statsServerUrl + "/hit", hitDto, Object.class));
    }

    public List<StatsDto> getStatistics(LocalDateTime start,
                                        LocalDateTime end,
                                        List<String> uris,
                                        Boolean unique) {

        if (start == null || end == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Start and End parameters are required.");
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(statsServerUrl + "/stats");
        builder.queryParam("start", DTF.format(start));
        builder.queryParam("end", DTF.format(end));
        if (uris != null)
            builder.queryParam("uris", String.join(",", uris));
        if (unique != null)
            builder.queryParam("unique", unique);
        URI uri = builder.build(false).toUri();

        try {
            StatsDto[] stats = restTemplate.getForObject(uri, StatsDto[].class);

            if (stats != null) {
                return new ArrayList<>(Arrays.asList(stats));
            } else {
                return Collections.emptyList();
            }
        } catch (HttpClientErrorException.BadRequest ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.error("Error while fetching statistics: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }
}