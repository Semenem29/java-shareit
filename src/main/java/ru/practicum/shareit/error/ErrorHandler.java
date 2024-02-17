package ru.practicum.shareit.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.exception.*;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({ValidationException.class, ItemNotAvailableException.class,
            InvalidLocalDateTimeException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationError(final RuntimeException e) {
        return new ErrorResponse("Validation error: " + e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationError2(final MethodArgumentNotValidException e) {
        return new ErrorResponse("Validation error on controller level: " + e.getClass());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationError3(final ConstraintViolationException e) {
        return new ErrorResponse("Validation error on controller level: " + e.getClass());
    }

    @ExceptionHandler({UnsupportedStatusException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnsupportedStatusException(final RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({UserAlreadyExistException.class, ItemAlreadyExistException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleValidationError4(final RuntimeException e) {
        return new ErrorResponse("an object is already exist");
    }

    @ExceptionHandler({AttempToUpdateNotYourItemException.class, AccessIsDeniedException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEmptyObjectPassingError(final RuntimeException e) {
        return new ErrorResponse("You have no access to perfrom this operation");
    }

    @ExceptionHandler({UserNotExistException.class, ItemNotExistException.class, BookingIsNotExistException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotExistUserError(final RuntimeException e) {
        return new ErrorResponse("provided object not found: " + e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        return new ErrorResponse("Unexpected error, error class: " + e.getClass());
    }
}
