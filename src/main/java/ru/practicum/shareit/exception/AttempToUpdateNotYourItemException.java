package ru.practicum.shareit.exception;

public class AttempToUpdateNotYourItemException extends RuntimeException {
    public AttempToUpdateNotYourItemException(String message) {
        super(message);
    }
}
