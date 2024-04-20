package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.model.request.Request;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.model.event.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RequestMapper {

    public Request toRequest(Event event, User requester, Request.Status status) {
        return Request.builder()
                .created(LocalDateTime.now())
                .status(status)
                .requester(requester)
                .event(event)
                .build();
    }

    public RequestDto toRequestDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .requester(request.getRequester().getId())
                .event(request.getEvent().getId())
                .status(request.getStatus())
                .created(request.getCreated())
                .build();
    }

    public List<RequestDto> toListRequestDto(List<Request> participationRequestList) {
        return participationRequestList.stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }
}
