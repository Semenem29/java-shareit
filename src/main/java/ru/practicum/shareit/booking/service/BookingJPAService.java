package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingJPAService {
    BookingResponseDto createBooking(BookingDto bookingDto, Long bookerId);

    BookingResponseDto approveBookingStatus(Boolean approved, Long bookingId, Long ownerId);

    BookingResponseDto getBooking(Long bookingId, Long userId);

    List<BookingResponseDto> getBookingsByOwner(Long userId, String state, Integer from, Integer size);

    List<BookingResponseDto> getBookingsByBooker(Long bookerId, String state, Integer from, Integer size);
}
