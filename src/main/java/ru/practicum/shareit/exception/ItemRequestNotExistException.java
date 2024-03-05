package ru.practicum.shareit.exception;

public class ItemRequestNotExistException extends RuntimeException {
    public ItemRequestNotExistException(String message) {
        super(message);
    }
}
