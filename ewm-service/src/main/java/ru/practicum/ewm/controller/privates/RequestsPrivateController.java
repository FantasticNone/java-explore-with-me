package ru.practicum.ewm.controller.privates;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.dto.request.RequestListDto;
import ru.practicum.ewm.dto.request.RequestUpdateStatusDto;
import ru.practicum.ewm.exceptions.BadRequestException;
import ru.practicum.ewm.service.request.RequestService;
import ru.practicum.ewm.service.event.EventService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class RequestsPrivateController {
    private final RequestService requestService;
    private final EventService eventService;

    @PostMapping("/{userId}/requests")
    @ResponseStatus(value = HttpStatus.CREATED)
    public RequestDto postRequest(@PathVariable Long userId,
                                  @RequestParam (required = false) Long eventId) {
        if (eventId == null){
            throw new BadRequestException("Event is not exist");
        }
        log.info("Private: making request by user with id: {} to event with id: {}", userId, eventId);
        return requestService.createParticipation(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    public List<RequestDto> getRequest(@PathVariable long userId) {
        log.info("Private: getting user requests");
        return requestService.getUserRequests(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public RequestDto patchRequest(@PathVariable long userId,
                                   @PathVariable long requestId) {
        log.info("Private: cancelling user with id : {} request with id: {}", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<RequestDto> getEventRequests(@PathVariable long userId,
                                             @PathVariable long eventId) {
        log.info("Private: getting requests to user with id: {} event with id: {}", userId, eventId);
        return eventService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public RequestListDto patchEventRequests(@PathVariable long userId,
                                             @PathVariable long eventId,
                                             @RequestBody RequestUpdateStatusDto updateRequest) {
        log.info("Private: changing requests status (user id = {}, event id = {}, requests = {}",
                userId,
                eventId,
                updateRequest.getRequestIds());
        return requestService.updateEventRequests(userId, eventId, updateRequest);
    }
}