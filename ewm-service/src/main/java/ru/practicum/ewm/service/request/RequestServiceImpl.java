package ru.practicum.ewm.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.dto.request.RequestListDto;
import ru.practicum.ewm.dto.request.RequestUpdateStatusDto;
import ru.practicum.ewm.exceptions.ConditionsViolationException;
import ru.practicum.ewm.exceptions.ConflictDataException;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.request.Request;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.repository.EventsRepository;
import ru.practicum.ewm.repository.RequestsRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestsRepository requestRepository;
    private final EventsRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public RequestDto createParticipation(long userId, long eventId) {
        User user = checkUserId(userId);
        Event event = checkEventId(eventId);

        if (!event.getState().equals(EventState.PUBLISHED))
            throw new ConflictDataException("Event is not published");

        if (event.getRequests().stream()
                .map(Request::getRequester)
                .anyMatch(requester -> requester.equals(user))) {
            throw new ConflictDataException("User already registered for participation in event");
        }
        if (event.getInitiator().equals(user))
            throw new ConflictDataException("User is initiator of this event");

        if ((event.getConfirmedRequests() >= event.getParticipantLimit())
                && (event.getConfirmedRequests() != 0 && event.getParticipantLimit() != 0)) {
            throw new ConditionsViolationException("Participation limit is overflowed");
        }

        Request.Status status;
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            status = Request.Status.CONFIRMED;
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else {
            status = Request.Status.PENDING;
        }
        Request request = RequestMapper.toRequest(event, user, status);
        requestRepository.save(request);
        eventRepository.save(event);
        return RequestMapper.toRequestDto(request);
    }

    @Override
    public List<RequestDto> getUserRequests(long userId) {
        User user = checkUserId(userId);
        List<Request> requests = requestRepository.findAllByRequester(user);

        return RequestMapper.toListRequestDto(requests);
    }

    @Override
    public RequestDto cancelRequest(long userId, long requestId) {
        checkUserId(userId);
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Participation request with id=%d was not found", requestId)));

        requestRepository.delete(request);

        request.setStatus(Request.Status.CANCELED);

        return RequestMapper.toRequestDto(request);
    }

    @Override
    public RequestListDto updateEventRequests(long userId, long eventId, RequestUpdateStatusDto updateRequest) {
        User user = checkUserId(userId);
        Event event = checkEventId(eventId);

        if (!event.getInitiator().getId().equals(user.getId()))
            throw new ConflictDataException("Only event initiator can update this event");

        if (event.getConfirmedRequests() >= event.getParticipantLimit())
            throw new ConflictDataException("Event's participant limit is full");

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictDataException("Participation  status is not pending");
        }

        List<Request> requests = requestRepository.findRequestsByIds(updateRequest.getRequestIds());
        List<RequestDto> confirmedRequest = new ArrayList<>();
        List<RequestDto> rejectedRequest = new ArrayList<>();

        for (Request request : requests) {
            if (updateRequest.getStatus().equals(Request.Status.CONFIRMED)) {
                if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
                    request.setStatus(Request.Status.CONFIRMED);
                    confirmedRequest.add(RequestMapper.toRequestDto(request));
                } else if (event.getConfirmedRequests() < event.getParticipantLimit()) {
                    request.setStatus(Request.Status.CONFIRMED);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                    confirmedRequest.add(RequestMapper.toRequestDto(request));
                } else {
                    request.setStatus(Request.Status.REJECTED);
                    rejectedRequest.add(RequestMapper.toRequestDto(request));
                }
            } else {
                request.setStatus(Request.Status.REJECTED);
                rejectedRequest.add(RequestMapper.toRequestDto(request));
            }
            requestRepository.save(request);
        }

        eventRepository.save(event);

        return RequestListDto.builder()
                .confirmedRequests(confirmedRequest)
                .rejectedRequests(rejectedRequest)
                .build();
    }

    private User checkUserId(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        return user;
    }

    private Event checkEventId(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
    }
}
