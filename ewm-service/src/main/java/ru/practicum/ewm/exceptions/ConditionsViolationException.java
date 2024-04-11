package ru.practicum.ewm.exceptions;

public class ConditionsViolationException extends RuntimeException {
    public ConditionsViolationException(String message) {
        super(message);
    }
}
