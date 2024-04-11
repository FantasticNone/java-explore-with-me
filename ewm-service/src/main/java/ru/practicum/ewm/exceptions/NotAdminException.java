package ru.practicum.ewm.exceptions;

public class NotAdminException extends RuntimeException {
    public NotAdminException(String message) {
        super(message);
    }
}
