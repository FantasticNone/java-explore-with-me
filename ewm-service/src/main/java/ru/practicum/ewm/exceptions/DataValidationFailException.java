package ru.practicum.ewm.exceptions;

public class DataValidationFailException extends RuntimeException {
    public DataValidationFailException(String message) {
        super(message);
    }
}