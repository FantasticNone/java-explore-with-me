package ru.practicum.ewm.service.request;

import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.dto.request.RequestListDto;
import ru.practicum.ewm.dto.request.RequestUpdateStatusDto;

import java.util.List;

public interface RequestService {

    RequestDto createParticipation(long userId, long eventId);

    List<RequestDto> getUserRequests(long userId);

    RequestDto cancelRequest(long userId, long requestId);

    RequestListDto updateEventRequests(long userId, long eventId, RequestUpdateStatusDto updateRequest);
}
