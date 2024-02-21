package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingJPAService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {
    private final BookingJPAService bookingService;

    @PostMapping
    public BookingResponseDto createBooking(@NotNull @RequestHeader("X-Sharer-User-Id") Long bookerId,
                                            @RequestBody @Valid BookingRequestDto bookingRequestDto) {

        log.info("POST-request: creating request with userId : {}, and booking:  {}", bookerId, bookingRequestDto);
        return bookingService.createBooking(bookingRequestDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBookingStatus(@NotNull @RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                   @RequestParam("approved") Boolean approved,
                                                   @PathVariable("bookingId") Long bookingId) {

        log.info("PATCH-request: approving request for bookingId={}, from userId : {}, and approved:  {}",
                bookingId, ownerId, approved);
        return bookingService.approveBookingStatus(approved, bookingId, ownerId);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBooking(@NotNull @RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long bookingId) {
        log.info("GET-request: get booking with bookingId={} by user with id={}", bookingId, userId);
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping("/owner")
    public Collection<BookingResponseDto> getBookingsByOwner(@NotNull @RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                             @RequestParam(value = "state",
                                                                     defaultValue = "ALL") String state) {
        log.info("GET-request: get booking collection by owner with id={} and state={}", ownerId, state);
        return bookingService.getBookingsByOwner(ownerId, state);
    }

    @GetMapping
    public Collection<BookingResponseDto> getBookingsByBooker(@NotNull @RequestHeader("X-Sharer-User-Id") Long bookerId,
                                                              @RequestParam(value = "state",
                                                                      defaultValue = "ALL") String state) {
        log.info("GET-request: get booking collection by booker with id={} and state={}", bookerId, state);
        return bookingService.getBookingsByBooker(bookerId, state);
    }
}
