package ru.practicum.shareit.exception;

public class ItemNotExistException extends RuntimeException {
    public ItemNotExistException(String message) {
        super(message);
    }
}
