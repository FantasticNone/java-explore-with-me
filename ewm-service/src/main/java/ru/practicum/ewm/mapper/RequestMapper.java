package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.RequestDto;
import ru.practicum.ewm.model.Request;

@UtilityClass
public class RequestMapper {
    public static RequestDto toRequestDto(Request participationRequest) {
        return RequestDto.builder()
                .id(participationRequest.getId())
                .requester(participationRequest.getRequester().getId())
                .event(participationRequest.getEvent().getId())
                .status(participationRequest.getStatus())
                .created(participationRequest.getCreated())
                .build();
    }
}
