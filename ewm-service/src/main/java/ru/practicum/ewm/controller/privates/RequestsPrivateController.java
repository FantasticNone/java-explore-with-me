package ru.practicum.ewm.controller.privates;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.RequestDto;
import ru.practicum.ewm.service.RequestService;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class RequestsPrivateController {
    private final RequestService requestService;

    @PostMapping("/{userId}/requests")
    @ResponseStatus(value = HttpStatus.CREATED)
    public RequestDto postRequest(@PathVariable long userId,
                                  @RequestParam long eventId) {
        log.info("Register for participation (user id = {}, event id = {})", userId, eventId);
        return requestService.createParticipation(userId, eventId);
    }
}