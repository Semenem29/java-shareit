package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationError(final ValidationException e) {
        return new ErrorResponse("Validation error");
    }

    @ExceptionHandler({UserAlreadyExistException.class, ItemAlreadyExistException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleValidationError(final RuntimeException e) {
        return new ErrorResponse("an object is already exist");
    }

    @ExceptionHandler({AttempToUpdateNotYourItemException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEmptyObjectPassingError(final AttempToUpdateNotYourItemException e) {
        return new ErrorResponse("Not yours!!!");
    }

    @ExceptionHandler({UserNotExistException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotExistUserError(final UserNotExistException e) {
        return new ErrorResponse("provided user not found");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        return new ErrorResponse("Unexpected error");
    }
}
