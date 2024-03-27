package ru.practicum.stats.server.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.stats.server.model.EndpointHit;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class EndpointHitMapper {

    public static EndpointHit toEndpointHit(HitDto hitDto) {
        return EndpointHit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(hitDto.getTimestamp())
                .build();
    }

    public static List<StatsDto> toResponse(List<EndpointHit> requests, boolean isUnique) {
        List<StatsDto> responses;

        Map<String, List<EndpointHit>> requestsByUri = requests.stream()
                .collect(Collectors.groupingBy(EndpointHit::getUri));

        if (!isUnique) {
            responses = requestsByUri.keySet().stream()
                    .map(key -> new StatsDto(requestsByUri.get(key).get(0).getApp(), key, (long) requestsByUri.get(key).size()))
                    .collect(Collectors.toList());
        } else {
            responses = requestsByUri.keySet().stream()
                    .map(key -> new StatsDto(requestsByUri.get(key).get(0).getApp(), key, requestsByUri.get(key).stream()
                            .map(EndpointHit::getIp)
                            .distinct()
                            .count()))
                    .collect(Collectors.toList());
        }

        responses.sort(Comparator.comparing(StatsDto::getHits).reversed());

        return responses;
    }
}
