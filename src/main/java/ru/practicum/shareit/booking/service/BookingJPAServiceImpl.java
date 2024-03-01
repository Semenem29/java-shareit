package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.store.BookingJPARepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemJPARepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJPARepository;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.practicum.shareit.booking.model.BookingStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingJPAServiceImpl implements BookingJPAService {

    private final BookingJPARepository bookingJPARepository;
    private final ItemJPARepository itemJPARepository;
    private final UserJPARepository userJPARepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingDto bookingDto, Long bookerId) {
        validateDateAndTime(bookingDto.getStart(), bookingDto.getEnd());
        Item item = getItemOrThrow(bookingDto.getItemId());
        checkIsItemAvailable(item);
        User owner = getUserOrThrow(bookerId);
        checkIsNotOwnerOrThrow(item, bookerId);

        Booking booking = BookingMapper.toBooking(bookingDto, owner, item, BookingStatus.WAITING);
        Booking savedBooking = bookingJPARepository.save(booking);
        log.info(String.format("booking is completed: ", savedBooking));

        return BookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBookingStatus(Boolean approved, Long bookingId, Long ownerId) {
        Booking booking = validateItemOwnerAndGetBooking(ownerId, bookingId);
        checkIfBookingStatusIsWaiting(booking);
        BookingStatus bookingStatus = getBookingStatusByApproved(approved);
        booking.setStatus(bookingStatus);

        bookingJPARepository.save(booking);
        log.info("after approving process the booking: {}", booking);
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBooking(Long bookingId, Long userId) {
        Booking booking = getBookingOrThrow(bookingId);
        User user = getUserOrThrow(userId);
        if (isBooker(user, booking) || isOwner(user.getId(), booking.getItem())) {
            BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(booking);
            log.info("booking info: {}, was provided to user with id={}", bookingResponseDto, userId);
            return bookingResponseDto;
        }
        String message = "for getting booking info you must be the item owner or the booker";
        log.error("AccessIsDeniedException: " + message);
        throw new AccessIsDeniedException(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByOwner(Long ownerId, String state, Integer from, Integer size) {
        getUserOrThrow(ownerId);
        BookingState bookingState = getBookingStateOrThrow(state);
        LocalDateTime now = LocalDateTime.now();
        Collection<Booking> ownerBookings;
        int page = from / size;
        Pageable pageRequest = PageRequest.of(page, size);

        switch (bookingState) {
            case ALL:
                ownerBookings = bookingJPARepository.findAllByItemOwnerIdOrderByStartDesc(ownerId, pageRequest);
                break;

            case PAST:
                ownerBookings = bookingJPARepository
                        .findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(ownerId, now, pageRequest);
                break;

            case CURRENT:
                ownerBookings = bookingJPARepository
                        .findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(ownerId, now, now, pageRequest);
                break;

            case FUTURE:
                ownerBookings = bookingJPARepository
                        .findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(ownerId, now, pageRequest);
                break;

            case REJECTED:
                List<BookingStatus> rejectedStatuses = List.of(REJECTED, CANCELED);
                ownerBookings = bookingJPARepository
                        .findAllByItemOwnerIdAndStatusInOrderByStartDesc(ownerId, rejectedStatuses, pageRequest);
                break;

            case WAITING:
                ownerBookings = bookingJPARepository
                        .findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, WAITING, pageRequest);
                break;
            default:
                String message = "Unknown state: UNSUPPORTED_STATUS";
                log.error("UnsupportedStatusException: " + message);
                throw new UnsupportedStatusException(message);
        }

        log.info("provided list of bookings with size={}", ownerBookings.size());
        return BookingMapper.toBookingResponseDtoList(ownerBookings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByBooker(Long bookerId, String state, Integer from, Integer size) {
        getUserOrThrow(bookerId);
        BookingState bookingState = getBookingStateOrThrow(state);
        LocalDateTime now = LocalDateTime.now();
        Collection<Booking> bookerBookings;
        int page  = from / size;
        Pageable pageRequest = PageRequest.of(page, size);

        switch (bookingState) {
            case ALL:
                bookerBookings = bookingJPARepository.findAllByBookerIdOrderByStartDesc(bookerId, pageRequest);
                break;

            case PAST:
                bookerBookings = bookingJPARepository
                        .findAllByBookerIdAndEndIsBeforeOrderByStartDesc(bookerId, now, pageRequest);
                break;

            case CURRENT:
                bookerBookings = bookingJPARepository
                        .findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(bookerId, now, now, pageRequest);
                break;

            case FUTURE:
                bookerBookings = bookingJPARepository
                        .findAllByBookerIdAndStartIsAfterOrderByStartDesc(bookerId, now, pageRequest);
                break;

            case REJECTED:
                List<BookingStatus> rejectedStatuses = List.of(REJECTED, CANCELED);
                bookerBookings = bookingJPARepository
                        .findAllByBookerIdAndStatusInOrderByStartDesc(bookerId, rejectedStatuses, pageRequest);
                break;

            case WAITING:
                bookerBookings = bookingJPARepository
                        .findAllByBookerIdAndStatusOrderByStartDesc(bookerId, WAITING, pageRequest);
                break;

            default:
                String message = "Unknown state: UNSUPPORTED_STATUS";
                log.error("UnsupportedStatusException: " + message);
                throw new UnsupportedStatusException(message);
        }

        log.info("provided list of bookings with size={}", bookerBookings.size());
        return BookingMapper.toBookingResponseDtoList(bookerBookings);

    }

    private BookingState getBookingStateOrThrow(String state) {
        BookingState boookingState;
        try {
            boookingState = BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            String message = "Unknown state: UNSUPPORTED_STATUS";
            log.error("UnsupportedStatusException: " + message);
            throw new UnsupportedStatusException(message);
        }

        return boookingState;
    }

    private void checkIsNotOwnerOrThrow(Item item, Long bookerId) {
        if (isOwner(bookerId, item)) {
            String message = "the owner can not book his/her own thing";
            log.error("AccessIsDeniedException: " + message);
            throw new AccessIsDeniedException(message);
        }
    }

    private void checkIsItemAvailable(Item item) {
        if (!item.getAvailable()) {
            String message = "the item is unavailable";
            log.error("ItemNotAvailableException: " + message);
            throw new ItemNotAvailableException(message);
        }
    }

    private void validateDateAndTime(LocalDateTime start, LocalDateTime end) {
        if (Objects.equals(start, null) || Objects.equals(end, null)) {
            String message = "to create booking you must provide the start and the end datetime of the booking";
            log.error("ValidationException: " + message);
            throw new ValidationException(message);
        }

        if (start.equals(end) || start.isAfter(end)) {
            String message = "incorrect date and time for start and end of the booking";
            log.error("InvalidLocalDateTimeException: " + message);
            throw new InvalidLocalDateTimeException(message);
        }

        if (start.isBefore(LocalDateTime.now())) {
            String message = "incorrect date and time for the start of the booking";
            log.error("InvalidLocalDateTimeException: " + message);
            throw new InvalidLocalDateTimeException(message);
        }
    }

    private Booking getBookingOrThrow(Long bookingId) {
        Optional<Booking> bookingOpt = bookingJPARepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            String message = "there is no booking with id: " + bookingId;
            log.error("BookingIsNotExistException: " + message);
            throw new BookingIsNotExistException(message);
        }

        return bookingOpt.get();
    }

    private User getUserOrThrow(Long userId) {
        Optional<User> userOpt = userJPARepository.findById(userId);
        if (userOpt.isEmpty()) {
            String message = "there is no user with id: " + userId;
            log.error("UserNotExistException: " + message);
            throw new UserNotExistException(message);
        }

        return userOpt.get();
    }

    private Boolean isBooker(User booker, Booking booking) {
        return Objects.equals(booker.getId(), booking.getBooker().getId());
    }


    private Booking validateItemOwnerAndGetBooking(Long ownerId, Long bookingId) {
        User owner = getUserOrThrow(ownerId);
        Booking booking = getBookingOrThrow(bookingId);
        if (!isOwner(owner.getId(), booking.getItem())) {
            String message = "to set booking status you must be the owner!!!";
            log.error("AccessIsDeniedException: " + message);
            throw new AccessIsDeniedException(message);
        }

        return booking;
    }

    private Boolean isOwner(Long ownerId, Item item) {

        return Objects.equals(ownerId, item.getOwner().getId());
    }

    private Item getItemOrThrow(Long itemId) {
        Optional<Item> itemOpt = itemJPARepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            String message = "there is no item with id: " + itemId;
            log.error("ItemNotExistException: " + message);
            throw new ItemNotExistException(message);
        }

        return itemOpt.get();
    }

    private void checkIfBookingStatusIsWaiting(Booking booking) {
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            String message = "you can not change previous booking status: " + booking.getStatus();
            log.error("ItemNotAvailableException: " + message);
            throw new ItemNotAvailableException(message);
        }
        log.info("booking status \'WAITING\' is correct");
    }

    private BookingStatus getBookingStatusByApproved(Boolean approved) {
        if (approved.equals(true)) {
            return APPROVED;
        }
        return REJECTED;
    }
}
