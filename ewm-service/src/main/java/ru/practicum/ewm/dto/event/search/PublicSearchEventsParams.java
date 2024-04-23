package ru.practicum.ewm.dto.event.search;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class PublicSearchEventsParams {

    private String text;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private String sort;
    private Integer from;
    private Integer size;
}