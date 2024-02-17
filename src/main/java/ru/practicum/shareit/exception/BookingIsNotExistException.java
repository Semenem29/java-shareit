package ru.practicum.shareit.exception;

public class BookingIsNotExistException extends RuntimeException {
    public BookingIsNotExistException(String message) {
        super(message);
    }
}
