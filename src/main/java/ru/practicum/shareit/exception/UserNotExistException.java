package ru.practicum.shareit.exception;

public class UserNotExistException extends RuntimeException {
    public UserNotExistException(String message) {
        super(message);
    }
}
