package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.location.EventLocationDto;
import ru.practicum.ewm.model.EventLocation;
import ru.practicum.ewm.model.event.Location;

@UtilityClass
public class EventLocationMapper {
    public Location toLocation(EventLocation eventLocation) {
        return Location.builder()
                .lon(eventLocation.getLon())
                .lat(eventLocation.getLat())
                .build();
    }
}