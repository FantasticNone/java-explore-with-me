package ru.practicum.http.client.hit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import ru.practicum.http.client.base.BaseClient;
import ru.practicum.dto.HitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient extends BaseClient {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsClient(@Value("${stat.server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createHit(HitDto dto) {
        return post(dto);
    }

    public ResponseEntity<Object> getStatistic(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String startString = start.format(DTF);
        String endString = end.format(DTF);
        StringBuilder url = new StringBuilder("?");

        if (unique == null) {
            unique = false;
        }
        if (uris != null) {
            for (String uri : uris) {
                url.append("&uris=").append(uri);
            }
            url.append("&");
        }

        Map<String, Object> parameters = Map.of(
                "start", startString,
                "end", endString,
                "unique", unique
        );

        return get("/stats" + url + "start={start}&end={end}&unique={unique}", parameters);
    }
}