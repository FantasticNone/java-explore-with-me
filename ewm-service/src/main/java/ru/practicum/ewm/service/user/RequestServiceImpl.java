package ru.practicum.ewm.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.RequestDto;
import ru.practicum.ewm.exceptions.ConditionsViolationException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.repository.EventsRepository;
import ru.practicum.ewm.repository.RequestsRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestsRepository requestRepository;
    private final EventsRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RequestDto createParticipation(long userId, long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        if (!event.getState().equals(EventState.PUBLISHED))
            throw new ConditionsViolationException("Event is not published");

        if (event.getRequests().stream()
                .map(Request::getRequester)
                .anyMatch(requester -> requester.equals(user))) {
            throw new ConditionsViolationException("User already registered for participation in event");
        }
        if (event.getInitiator().equals(user))
            throw new ConditionsViolationException("User is initiator of this event");
        if (!event.getParticipantLimit().equals(0L)) {
            List<Request> approvedRequests = event.getRequests().stream()
                    .filter(r -> r.getStatus().equals(Request.Status.CONFIRMED))
                    .collect(Collectors.toList());

            if (approvedRequests.size() == event.getParticipantLimit())
                throw new ConditionsViolationException("Participation limit is overflowed");
        }

        Request request = Request.builder()
                .requester(user)
                .created(LocalDateTime.now())
                .status(Request.Status.PENDING)
                .event(event)
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit().equals(0L))
            request.setStatus(Request.Status.CONFIRMED);


            Request newParticipationRequest = requestRepository.save(request);
            return RequestMapper.toRequestDto(newParticipationRequest);

        }
}
