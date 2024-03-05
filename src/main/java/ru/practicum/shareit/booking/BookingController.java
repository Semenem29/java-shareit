package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingJPAService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
@Slf4j
@Validated
public class BookingController {
    private final BookingJPAService bookingService;

    @PostMapping
    public BookingResponseDto createBooking(@NotNull @RequestHeader("X-Sharer-User-Id") Long bookerId,
                                            @RequestBody @Valid BookingDto bookingDto) {

        log.info("POST-request: creating request with userId : {}, and booking:  {}", bookerId, bookingDto);
        return bookingService.createBooking(bookingDto, bookerId);
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
    public List<BookingResponseDto> getByOwner(@NotNull @RequestHeader("X-Sharer-User-Id") Long ownerId,
                                               @RequestParam(value = "state",
                                                       defaultValue = "ALL") String state,
                                               @PositiveOrZero @RequestParam(
                                                       value = "from", defaultValue = "0") Integer from,
                                               @Positive @RequestParam(
                                                       value = "size", defaultValue = "10") Integer size
    ) {
        log.info("GET-request: get booking collection by owner with id={}, and state={}, from={}, size={}",
                ownerId, state, from, size);
        return bookingService.getBookingsByOwner(ownerId, state, from, size);
    }

    @GetMapping
    public List<BookingResponseDto> getByBooker(@NotNull @RequestHeader("X-Sharer-User-Id") Long bookerId,
                                                @RequestParam(value = "state",
                                                        defaultValue = "ALL") String state,
                                                @PositiveOrZero @RequestParam(
                                                        value = "from", defaultValue = "0") Integer from,
                                                @Positive @RequestParam(
                                                        value = "size", defaultValue = "10") Integer size) {
        log.info("GET-request: get booking collection by booker with id={} and state={}", bookerId, state);
        return bookingService.getBookingsByBooker(bookerId, state, from, size);
    }
}
