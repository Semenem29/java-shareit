package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.Collection;

public interface BookingJPAService {
    BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long bookerId);

    BookingResponseDto approveBookingStatus(Boolean approved, Long bookingId, Long ownerId);

    BookingResponseDto getBooking(Long bookingId, Long userId);

    Collection<BookingResponseDto> getBookingsByOwner(Long userId, String state);

    Collection<BookingResponseDto> getBookingsByBooker(Long bookerId, String state);
}
