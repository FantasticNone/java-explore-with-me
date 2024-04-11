package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.RequestDto;

public interface RequestService {

    RequestDto createParticipation(long userId, long eventId);
}
