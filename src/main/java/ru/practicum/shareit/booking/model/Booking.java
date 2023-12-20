package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Item item;
    private User booker;
    private Status status;
    private Boolean available;


    public boolean isAvailable() {
        if (status == Status.APPROVED) {
            return false;
        }
        return true;
    }
}
