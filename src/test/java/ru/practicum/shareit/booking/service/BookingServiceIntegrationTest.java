package ru.practicum.shareit.booking.service;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemJPAService;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestJPAService;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserJPAService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookingServiceIntegrationTest {

    @Autowired
    UserJPAService userService;
    @Autowired
    ItemJPAService itemService;
    @Autowired
    BookingJPAService bookingService;
    @Autowired
    ItemRequestJPAService itemRequestService;

    @Test
    public void shouldCreateBooking() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.createUser(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        UserDto savedBookerDto = userService.createUser(bookerDto);
        User booker = UserMapper.toUser(savedBookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        ItemRequestResponseDto savedRequestDto = itemRequestService.createRequest(bookerId, itemRequestDto);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(savedRequestDto, booker);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endAfterStart = start.plusWeeks(1);

        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(endAfterStart)
                .itemId(itemId)
                .build();

        BookingResponseDto result = bookingService.createBooking(bookingDto, bookerId);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", endAfterStart)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "a spoon")
                .hasFieldOrPropertyWithValue("item.description", "new")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.owner", owner)
                .hasFieldOrPropertyWithValue("item.owner.id", ownerId)
                .hasFieldOrPropertyWithValue("item.owner.name", owner.getName())
                .hasFieldOrPropertyWithValue("item.owner.email", owner.getEmail())
                .hasFieldOrPropertyWithValue("item.request.id", requestId)
                .hasFieldOrPropertyWithValue("item.request.description", itemRequest.getDescription())
                .hasFieldOrPropertyWithValue("item.request.requester.id", bookerId)
                .hasFieldOrPropertyWithValue("item.request.requester.name", booker.getName())
                .hasFieldOrPropertyWithValue("item.request.requester.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", booker.getName())
                .hasFieldOrPropertyWithValue("booker.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING);
    }

    @Test
    public void shouldFailCreateIfStartEqualsEnd() {

        Long bookerId = 1L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(start)
                .build();

        assertThrows(InvalidLocalDateTimeException.class,
                () -> bookingService.createBooking(bookingDto, bookerId),
                "incorrect date and time for start and end of the booking");

    }

    @Test
    public void shouldFailCreateIfEndIsBeforeStart() {

        Long bookerId = 1L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endBeforeStart = start.minusYears(1);

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(endBeforeStart)
                .build();

        assertThrows(InvalidLocalDateTimeException.class,
                () -> bookingService.createBooking(bookingDto, bookerId),
                "incorrect date and time for start and end of the booking");
    }

    @Test
    public void shouldFailCreateIfItemIsNotFound() {

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        Long itemId = 1L;

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endAfterStart = start.plusWeeks(1);

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(endAfterStart)
                .itemId(itemId)
                .build();

        assertThrows(ItemNotExistException.class,
                () -> bookingService.createBooking(bookingDto, bookerId),
                String.format("there is no item with id: %s", itemId));
    }

    @Test
    public void shouldFailCreateIfItemIsNotAvailable() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        itemRequestService.createRequest(bookerId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(false)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endAfterStart = start.plusWeeks(1);

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(endAfterStart)
                .itemId(itemId)
                .build();

        assertThrows(ItemNotAvailableException.class,
                () -> bookingService.createBooking(bookingDto, bookerId),
                "the item is unavailable");
    }

    @Test
    public void shouldFailCreateIfUserNotFound() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long bookerId = 2L;

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endAfterStart = start.plusWeeks(1);

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(endAfterStart)
                .itemId(itemId)
                .build();

        assertThrows(UserNotExistException.class,
                () -> bookingService.createBooking(bookingDto, bookerId),
                String.format("there is no user with id: %s", bookerId));
    }

    @Test
    public void shouldFailCreateIfUserIsOwner() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endAfterStart = start.plusWeeks(1);

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(endAfterStart)
                .itemId(itemId)
                .build();

        assertThrows(AccessIsDeniedException.class,
                () -> bookingService.createBooking(bookingDto, ownerId),
                "the owner can not book his/her own thing");
    }

    @Test
    public void shouldGetBookingByIdForOwner() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.createUser(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        UserDto savedBookerDto = userService.createUser(bookerDto);
        User booker = UserMapper.toUser(savedBookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        ItemRequestResponseDto savedRequestDto = itemRequestService.createRequest(bookerId, itemRequestDto);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(savedRequestDto, booker);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto, bookerId);

        BookingResponseDto result = bookingService.getBooking(ownerId, bookingId);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "a spoon")
                .hasFieldOrPropertyWithValue("item.description", "new")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.owner", owner)
                .hasFieldOrPropertyWithValue("item.owner.id", ownerId)
                .hasFieldOrPropertyWithValue("item.owner.name", owner.getName())
                .hasFieldOrPropertyWithValue("item.owner.email", owner.getEmail())
                .hasFieldOrPropertyWithValue("item.request.id", requestId)
                .hasFieldOrPropertyWithValue("item.request.description", itemRequest.getDescription())
                .hasFieldOrPropertyWithValue("item.request.requester.id", bookerId)
                .hasFieldOrPropertyWithValue("item.request.requester.name", booker.getName())
                .hasFieldOrPropertyWithValue("item.request.requester.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", booker.getName())
                .hasFieldOrPropertyWithValue("booker.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING);
    }

    @Test
    public void shouldGetBookingByIdForBooker() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.createUser(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        UserDto savedBookerDto = userService.createUser(bookerDto);
        User booker = UserMapper.toUser(savedBookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        ItemRequestResponseDto savedRequestDto = itemRequestService.createRequest(bookerId, itemRequestDto);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(savedRequestDto, booker);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto, bookerId);

        BookingResponseDto result = bookingService.getBooking(bookingId, bookerId);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "a spoon")
                .hasFieldOrPropertyWithValue("item.description", "new")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.owner", owner)
                .hasFieldOrPropertyWithValue("item.owner.id", ownerId)
                .hasFieldOrPropertyWithValue("item.owner.name", owner.getName())
                .hasFieldOrPropertyWithValue("item.owner.email", owner.getEmail())
                .hasFieldOrPropertyWithValue("item.request.id", requestId)
                .hasFieldOrPropertyWithValue("item.request.description", itemRequest.getDescription())
                .hasFieldOrPropertyWithValue("item.request.requester.id", bookerId)
                .hasFieldOrPropertyWithValue("item.request.requester.name", booker.getName())
                .hasFieldOrPropertyWithValue("item.request.requester.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", booker.getName())
                .hasFieldOrPropertyWithValue("booker.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING);
    }

    @Test
    public void shouldFailGetByIdIfUserIsNotOwnerOrBooker() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        Long otherId = 3L;
        UserDto otherUserDto = UserDto.builder()
                .name("Kate")
                .email("Kate@yandex.ru")
                .build();
        userService.createUser(otherUserDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        itemRequestService.createRequest(bookerId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto, bookerId);

        assertThrows(AccessIsDeniedException.class,
                () -> bookingService.getBooking(bookingId, otherId),
               "for getting booking info you must be the item owner or the booker");

    }

    @Test
    public void shouldUpdateStatusToApproved() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.createUser(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        UserDto savedBookerDto = userService.createUser(bookerDto);
        User booker = UserMapper.toUser(savedBookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        ItemRequestResponseDto savedRequestDto = itemRequestService.createRequest(bookerId, itemRequestDto);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(savedRequestDto, booker);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto, bookerId);

        Boolean approved = true;

        BookingResponseDto result = bookingService.approveBookingStatus(approved, bookingId, ownerId);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "a spoon")
                .hasFieldOrPropertyWithValue("item.description", "new")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.owner", owner)
                .hasFieldOrPropertyWithValue("item.owner.id", ownerId)
                .hasFieldOrPropertyWithValue("item.owner.name", owner.getName())
                .hasFieldOrPropertyWithValue("item.owner.email", owner.getEmail())
                .hasFieldOrPropertyWithValue("item.request.id", requestId)
                .hasFieldOrPropertyWithValue("item.request.description", itemRequest.getDescription())
                .hasFieldOrPropertyWithValue("item.request.requester.id", bookerId)
                .hasFieldOrPropertyWithValue("item.request.requester.name", booker.getName())
                .hasFieldOrPropertyWithValue("item.request.requester.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", booker.getName())
                .hasFieldOrPropertyWithValue("booker.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED);
    }

    @Test
    public void shouldUpdateStatusToRejected() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.createUser(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        UserDto savedBookerDto = userService.createUser(bookerDto);
        User booker = UserMapper.toUser(savedBookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        ItemRequestResponseDto savedRequestDto = itemRequestService.createRequest(bookerId, itemRequestDto);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(savedRequestDto, booker);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto, bookerId);

        Boolean approved = false;

        BookingResponseDto result = bookingService.approveBookingStatus(approved, bookingId, ownerId);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "a spoon")
                .hasFieldOrPropertyWithValue("item.description", "new")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.owner", owner)
                .hasFieldOrPropertyWithValue("item.owner.id", ownerId)
                .hasFieldOrPropertyWithValue("item.owner.name", owner.getName())
                .hasFieldOrPropertyWithValue("item.owner.email", owner.getEmail())
                .hasFieldOrPropertyWithValue("item.request.id", requestId)
                .hasFieldOrPropertyWithValue("item.request.description", itemRequest.getDescription())
                .hasFieldOrPropertyWithValue("item.request.requester.id", bookerId)
                .hasFieldOrPropertyWithValue("item.request.requester.name", booker.getName())
                .hasFieldOrPropertyWithValue("item.request.requester.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", booker.getName())
                .hasFieldOrPropertyWithValue("booker.email", booker.getEmail())
                .hasFieldOrPropertyWithValue("status", BookingStatus.REJECTED);

    }

    @Test
    public void shouldFailUpdateStatusIfUserDoesNotExists() {

        Long userId = -1L;

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);


        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        itemRequestService.createRequest(bookerId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto, bookerId);

        Boolean approved = false;

        assertThrows(UserNotExistException.class,
                () -> bookingService.approveBookingStatus(approved, bookingId, userId),
                String.format("there is no user with id: %s", userId));
    }

    @Test
    public void shouldFailUpdateStatusIfBookingDoesNotExist() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        itemRequestService.createRequest(bookerId, itemRequestDto);

        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        Long bookingId = 1L;

        Boolean approved = false;

        assertThrows(BookingIsNotExistException.class,
                () -> bookingService.approveBookingStatus(approved, bookingId, ownerId),
                String.format("there is no booking with id: %s", bookingId));

    }

    @Test
    public void shouldFailUpdateStatusIfUserIsNotOwner() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.createUser(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        UserDto savedBookerDto = userService.createUser(bookerDto);
        User booker = UserMapper.toUser(savedBookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        ItemRequestResponseDto savedRequestDto = itemRequestService.createRequest(bookerId, itemRequestDto);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(savedRequestDto, booker);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        ItemDto savedItemDto = itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto, bookerId);

        Boolean approved = true;

        assertThrows(AccessIsDeniedException.class,
                () -> bookingService.approveBookingStatus(approved, bookingId, bookerId),
                "to set booking status you must be the owner!!!");
    }

    @Test
    public void shouldFailUpdateStatusIfItIsNotWaiting() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        itemRequestService.createRequest(bookerId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        BookingResponseDto bookingOutDto = bookingService.createBooking(bookingDto, bookerId);
        Booking booking = BookingMapper.toBooking(bookingOutDto);

        Boolean approved = true;
        bookingService.approveBookingStatus(approved, bookingId, ownerId);

        assertThrows(ItemNotAvailableException.class,
                () -> bookingService.approveBookingStatus(approved, bookingId, ownerId),
                String.format("you can not change previous booking status: %s", booking.getStatus()));
    }

    @Test
    public void shouldGetListByOwnerAllBookings() {

        String state = "ALL";

        int from = 0;
        int size = 10;

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        itemRequestService.createRequest(bookerId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        BookingResponseDto bookingOutDto1 = bookingService.createBooking(bookingDto1, bookerId);
        Booking booking1 = BookingMapper.toBooking(bookingOutDto1);

        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        BookingResponseDto bookingOutDto2 = bookingService.createBooking(bookingDto2, bookerId);
        Booking booking2 = BookingMapper.toBooking(bookingOutDto2);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(ownerId, state, from, size);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.WAITING);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.WAITING);
    }
    @Test
    public void shouldGetListByOwnerFutureBookings() {

        String state = "FUTURE";

        int from = 0;
        int size = 10;

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        itemRequestService.createRequest(bookerId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        BookingResponseDto bookingOutDto1 = bookingService.createBooking(bookingDto1, bookerId);
        Booking booking1 = BookingMapper.toBooking(bookingOutDto1);

        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        BookingResponseDto bookingOutDto2 = bookingService.createBooking(bookingDto2, bookerId);
        Booking booking2 = BookingMapper.toBooking(bookingOutDto2);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(ownerId, state, from, size);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.WAITING);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.WAITING);
    }

    @Test
    public void shouldGetListByOwnerRejectedBookings() {

        String state = "FUTURE";

        int from = 0;
        int size = 10;

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        itemRequestService.createRequest(bookerId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto1, bookerId);
        bookingService.approveBookingStatus(false, bookingId1, ownerId);

        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto2, bookerId);
        bookingService.approveBookingStatus(false, bookingId2, ownerId);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(ownerId, state, from, size);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.REJECTED);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void shouldGetListByOwnerWaitingBookings() {

        String state = "WAITING";

        int from = 0;
        int size = 10;

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        itemRequestService.createRequest(bookerId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        BookingResponseDto bookingOutDto1 = bookingService.createBooking(bookingDto1, bookerId);
        Booking booking1 = BookingMapper.toBooking(bookingOutDto1);


        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        BookingResponseDto bookingOutDto2 = bookingService.createBooking(bookingDto2, bookerId);
        Booking booking2 = BookingMapper.toBooking(bookingOutDto2);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(ownerId, state, from, size);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.WAITING);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.WAITING);
    }
    @Test
    public void shouldFailGetListByUserNotFound() {

        Long ownerId = 1L;

        String state = "WAITING";

        int from = 0;
        int size = 10;

        assertThrows(UserNotExistException.class,
                () -> bookingService.getBookingsByOwner(ownerId, state, from, size),
                String.format("there is no user with id: %s", ownerId));
    }

    @Test
    public void shouldFailGetListByUserWithInvalidState() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        String state = "INVALID";

        int from = 0;
        int size = 10;

        assertThrows(UnsupportedStatusException.class,
                () -> bookingService.getBookingsByOwner(ownerId, state, from, size),
                "Unknown state: UNSUPPORTED_STATUS");
    }

    @Test
    public void shouldGetListByBookerAllBookings() {

        String state = "ALL";

        int from = 0;
        int size = 10;

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        itemRequestService.createRequest(bookerId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto1, bookerId);

        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto2, bookerId);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, from, size);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.WAITING);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.WAITING);

    }

    @Test
    public void shouldGetListByBookerFutureBookings() {

        String state = "FUTURE";

        int from = 0;
        int size = 10;

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        itemRequestService.createRequest(bookerId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        BookingResponseDto bookingOutDto1 = bookingService.createBooking(bookingDto1, bookerId);
        Booking booking1 = BookingMapper.toBooking(bookingOutDto1);

        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        BookingResponseDto bookingOutDto2 = bookingService.createBooking(bookingDto2, bookerId);
        Booking booking2 = BookingMapper.toBooking(bookingOutDto2);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, from, size);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.WAITING);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.WAITING);

    }

    @Test
    public void shouldGetListByBookerRejectedBookings() {

        String state = "REJECTED";

        int from = 0;
        int size = 10;

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        itemRequestService.createRequest(bookerId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto1, bookerId);
        bookingService.approveBookingStatus(false, bookingId1, ownerId);

        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto2, bookerId);
        bookingService.approveBookingStatus(false, bookingId2, ownerId);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, from, size);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.REJECTED);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void shouldGetListByBookerWaitingBookings() {

        String state = "WAITING";

        int from = 0;
        int size = 10;

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long bookerId = 2L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        itemRequestService.createRequest(bookerId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Long bookingId1 = 1L;
        BookingDto bookingDto1 = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto1, bookerId);

        Long bookingId2 = 2L;
        BookingDto bookingDto2 = BookingDto.builder()
                .start(start.plusWeeks(1))
                .end(end.plusWeeks(1))
                .itemId(itemId)
                .build();
        bookingService.createBooking(bookingDto2, bookerId);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, from, size);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        assertEquals(result.get(0).getId(), bookingId2);
        assertEquals(result.get(0).getStatus(), BookingStatus.WAITING);
        assertEquals(result.get(1).getId(), bookingId1);
        assertEquals(result.get(1).getStatus(), BookingStatus.WAITING);
    }

    @Test
    public void shouldFailGetListByBookerIfUserNotFound() {

        Long bookerId = 1L;

        String state = "WAITING";

        int from = 0;
        int size = 10;

        assertThrows(UserNotExistException.class,
                () -> bookingService.getBookingsByBooker(bookerId, state, from, size),
                String.format("there is no user with id: %s", bookerId));
    }

    @Test
    public void shouldFailGetListByBookerWhenInvalidState() {

        Long bookerId = 1L;
        UserDto bookerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(bookerDto);

        String state = "INVALID";

        int from = 0;
        int size = 10;

        assertThrows(UnsupportedStatusException.class,
                () -> bookingService.getBookingsByBooker(bookerId, state, from, size),
                "Unknown state: UNSUPPORTED_STATUS");
    }
}
