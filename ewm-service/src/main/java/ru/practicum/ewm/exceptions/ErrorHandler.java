package ru.practicum.ewm.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        log.debug("Получен статус 404 Not found {}", e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({ConditionsViolationException.class,
            ConflictDataException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictDataException(final Exception e) {
        log.debug("Получен статус 409 Conflict {}", e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class,
            BadRequestException.class,
            IncorrectStatusException.class,
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class,
            DataValidationFailException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidateException(final Exception e) {
        log.debug("Получен статус 400 Bad Request {}", e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        log.error("Получен статус 500 Internal Server Error {}", e.getMessage(), e);
        return new ErrorResponse("Internal Server Error");
    }
}
