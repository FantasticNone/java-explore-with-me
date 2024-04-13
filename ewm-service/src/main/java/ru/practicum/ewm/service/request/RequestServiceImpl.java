package ru.practicum.ewm.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.dto.request.RequestListDto;
import ru.practicum.ewm.dto.request.RequestUpdateStatusDto;
import ru.practicum.ewm.exceptions.ConditionsViolationException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.model.RequestStatus;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.repository.EventsRepository;
import ru.practicum.ewm.repository.RequestsRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Override
    public List<RequestDto> getUserRequests(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));

        List<Request> requests = requestRepository.findAllByUser(user);

        return RequestMapper.toListRequestDto(requests);
    }

    @Override
    public RequestDto cancelRequest(long userId, long requestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Participation request with id=%d was not found", requestId)));

        requestRepository.delete(request);

        request.setStatus(Request.Status.CANCELED);

        return RequestMapper.toRequestDto(request);
    }

    @Override
    public RequestListDto updateEventRequests(long userId, long eventId, RequestUpdateStatusDto updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        if (!event.getInitiator().getId().equals(user.getId()))
            throw new ConditionsViolationException("Only event initiator can update this event");

        long approvedRequests = requestRepository.findAllByEventIdAndStatus(event.getId(), Request.Status.CONFIRMED).size();
        if (event.getParticipantLimit().equals(approvedRequests))
            throw new ConditionsViolationException("Event's participant limit is full");

        List<Request> eventRequests = event.getRequests();
        RequestListDto updateResult = new RequestListDto(new ArrayList<>(), new ArrayList<>());
        eventRequests.forEach(request -> {
            if (event.getParticipantLimit().equals(
                    eventRequests.stream()
                            .filter(e -> e.getStatus().equals(Request.Status.CONFIRMED))
                            .count()
            )) {
                return;
            }

            if (updateRequest.getRequestIds().contains(request.getId())) {
                if (request.getStatus().equals(Request.Status.PENDING)) {
                    request.setStatus(updateRequest.getStatus().equals(
                            RequestStatus.CONFIRMED) ? Request.Status.CONFIRMED : Request.Status.REJECTED
                    );

                    if (request.getStatus().equals(Request.Status.CONFIRMED))
                        updateResult.getConfirmedRequests().add(RequestMapper.toRequestDto(request));
                    else
                        updateResult.getRejectedRequests().add(RequestMapper.toRequestDto(request));
                } else {
                    throw new ConditionsViolationException(String.format("Participation id=%d status is not pending", request.getId()));
                }
            }
        });

        approvedRequests = requestRepository.findAllByEventIdAndStatus(event.getId(), Request.Status.CONFIRMED).size();
        if (event.getParticipantLimit().equals(approvedRequests)) {
            eventRequests.forEach(request -> {
                if (request.getStatus().equals(Request.Status.PENDING)) {
                    request.setStatus(Request.Status.REJECTED);

                    updateResult.getRejectedRequests().add(RequestMapper.toRequestDto(request));
                }
            });
        }

            eventRepository.save(event);

        return updateResult;
    }
}
