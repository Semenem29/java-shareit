package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingItemResponseDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {
    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                booking.getBooker(),
                booking.getItem()
        );
    }

    public static BookingItemResponseDto toBookingItemResponseDto(Booking booking) {
        return new BookingItemResponseDto(
                booking.getId(),
                booking.getBooker().getId()
        );
    }

    public static Booking toBooking(BookingDto BookingDto,
                                    User user, Item item, BookingStatus status) {
        return Booking.builder()
                .id(BookingDto.getId())
                .start(BookingDto.getStart())
                .end(BookingDto.getEnd())
                .booker(user)
                .item(item)
                .status(status)
                .build();
    }


    public static List<BookingResponseDto> toBookingResponseDtoList(Collection<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    public static BookingItemResponseDto toBookingItemResponseDto(BookingResponseDto bookingResponseDto) {
        return new BookingItemResponseDto(
                bookingResponseDto.getId(),
                bookingResponseDto.getBooker().getId()
        );
    }

}
