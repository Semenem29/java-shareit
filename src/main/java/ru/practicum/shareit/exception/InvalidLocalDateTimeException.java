package ru.practicum.shareit.exception;

public class InvalidLocalDateTimeException extends RuntimeException {
    public InvalidLocalDateTimeException(String message) {
        super(message);
    }
}
