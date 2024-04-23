package ru.practicum.ewm.dto.event.state;

import ru.practicum.ewm.exceptions.IncorrectStatusException;

public enum StateActionAdmin {
    PUBLISH_EVENT,
    REJECT_EVENT;

    public static StateActionAdmin toEnum(String action) {
        try {
            return StateActionAdmin.valueOf(action);
        } catch (RuntimeException exception) {
            throw new IncorrectStatusException(String.format("State is incorrect: %s", action));
        }
    }
}