package ru.practicum.shareit.booking.service;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.store.BookingJPARepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemJPARepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJPARepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    @InjectMocks
    private BookingJPAServiceImpl bookingService;
    @Mock
    private UserJPARepository userRepository;
    @Mock
    private ItemJPARepository itemRepository;
    @Mock
    private BookingJPARepository bookingRepository;

    @Test
    public void create_whenStartEndAreValid_ItemExists_ItemIsAvailable_UserExists_UserIsNotOwner_invokeSave_returnResult() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        Long bookerId = 2L;
        User booker = User.builder()
                .id(bookerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long itemId = 1L;
        Item item = Item.builder()
                .id(itemId)
                .name("a spoon")
                .description("silver")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endAfterStart = start.plusWeeks(1);

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(endAfterStart)
                .itemId(itemId)
                .build();

        Booking booking = BookingMapper.toBooking(bookingDto, booker, item, BookingStatus.WAITING);

        Long bookingId = 1L;
        Booking savedBooking = booking.toBuilder().id(bookingId).build();

        BookingResponseDto expectedBooking = BookingMapper.toBookingResponseDto(savedBooking);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.save(booking)).thenReturn(savedBooking);

        BookingResponseDto result = bookingService.createBooking(bookingDto, bookerId);

        InOrder inOrder = inOrder(itemRepository, userRepository, bookingRepository);
        inOrder.verify(itemRepository).findById(itemId);
        inOrder.verify(userRepository).findById(bookerId);
        inOrder.verify(bookingRepository).save(booking);

        assertEquals(result, expectedBooking);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", endAfterStart)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "a spoon")
                .hasFieldOrPropertyWithValue("item.description", "silver")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.request", null)
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", "Oleg")
                .hasFieldOrPropertyWithValue("booker.email", "Oleg@yandex.ru")
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING);

    }

    @Test
    public void create_whenStartEqualsEnd_thenThrowsInvalidLocalDateTimeException_NotInvokeAnyMore() {

        Long bookerId = 2L;

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(start)
                .build();

        assertThrows(InvalidLocalDateTimeException.class,
                () -> bookingService.createBooking(bookingDto, bookerId),
                "incorrect date and time for start and end of the booking");

        verifyNoInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    public void create_whenEndIsBeforeStart_thenThrowsInvalidLocalDateTimeException_NotInvokeAnyMore() {

        Long bookerId = 2L;

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endBeforeStart = start.minusYears(1);

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(endBeforeStart)
                .build();

        assertThrows(InvalidLocalDateTimeException.class,
                () -> bookingService.createBooking(bookingDto, bookerId),
                "incorrect date and time for start and end of the booking");

        verifyNoInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    public void create_whenStartEndAreValid_ItemDoesNotExist_thenThrowsItemNotExistException_NotInvokeAnyMore() {

        Long bookerId = 2L;

        Long itemId = 1L;

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endAfterStart = start.plusWeeks(1);

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(endAfterStart)
                .itemId(itemId)
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotExistException.class,
                () -> bookingService.createBooking(bookingDto, bookerId),
                String.format("there is no item with id: %s", itemId));

        verify(itemRepository).findById(itemId);
        verifyNoInteractions(userRepository, bookingRepository);
    }

    @Test
    public void create_whenStartEndAreValid_ItemExistButNotAvailable_thenThrowsItemNotAvailable_NotInvokeAnyMore() {

        Long bookerId = 2L;

        Long itemId = 1L;
        Item item = Item.builder()
                .id(itemId)
                .available(false)
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

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

        verify(itemRepository).findById(itemId);
        verifyNoInteractions(userRepository, bookingRepository);
    }

    @Test
    public void create_whenStartEndAreValid_ItemExistAndAvailable_UserNotExists_thenThrowsUserNotExist_NotInvoke() {

        Long bookerId = 2L;

        Long itemId = 1L;
        Item item = Item.builder()
                .id(itemId)
                .available(true)
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(bookerId)).thenReturn(Optional.empty());

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

        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(bookerId);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    public void create_whenStartEndAreValid_ItemExistsAndAvailable_UserExistsButIsOwner_thenThrowsAccessIsDenied() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        Long itemId = 1L;
        Item item = Item.builder()
                .id(itemId)
                .owner(owner)
                .available(true)
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

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

        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(ownerId);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    public void getById_whenBookingExists_whenUserIsOwner_thenReturnBooking() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        Long bookerId = 2L;
        User booker = User.builder()
                .id(bookerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long itemId = 1L;
        Item item = Item.builder()
                .id(itemId)
                .name("a spoon")
                .description("silver")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        Long bookingId = 1L;
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Booking booking = Booking.builder()
                .id(bookingId)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();


        BookingResponseDto expectedBooking = BookingMapper.toBookingResponseDto(booking);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBooking(bookingId, ownerId);

        verify(userRepository).findById(ownerId);
        verify(bookingRepository).findById(bookingId);

        assertEquals(result, expectedBooking);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "a spoon")
                .hasFieldOrPropertyWithValue("item.description", "silver")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.request", null)
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", "Oleg")
                .hasFieldOrPropertyWithValue("booker.email", "Oleg@yandex.ru")
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING);
    }

    @Test
    public void getById_whenBookingExists_whenUserIsBooker_thenReturnBooking() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        Long bookerId = 2L;
        User booker = User.builder()
                .id(bookerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long itemId = 1L;
        Item item = Item.builder()
                .id(itemId)
                .name("a spoon")
                .description("silver")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        Long bookingId = 1L;
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Booking booking = Booking.builder()
                .id(bookingId)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        BookingResponseDto expectedBooking = BookingMapper.toBookingResponseDto(booking);

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBooking(bookingId, bookerId);

        verify(userRepository).findById(bookerId);
        verify(bookingRepository).findById(bookingId);

        assertEquals(result, expectedBooking);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "a spoon")
                .hasFieldOrPropertyWithValue("item.description", "silver")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.request", null)
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", "Oleg")
                .hasFieldOrPropertyWithValue("booker.email", "Oleg@yandex.ru")
                .hasFieldOrPropertyWithValue("status", BookingStatus.WAITING);
    }

    @Test
    public void getById_whenBookingExists_whenUserIsNotBookerOrOwner_thenThrowsAccessIsDeniedException() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .build();

        Long bookerId = 2L;
        User booker = User.builder()
                .id(bookerId)
                .build();

        Long notAllowedUserId = 3L;
        User notAllowedUser = User.builder()
                .id(notAllowedUserId)
                .build();

        Long itemId = 1L;
        Item item = Item.builder()
                .id(itemId)
                .owner(owner)
                .build();

        Long bookingId = 1L;

        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findById(notAllowedUserId)).thenReturn(Optional.of(notAllowedUser));

        assertThrows(AccessIsDeniedException.class,
                () -> bookingService.getBooking(bookingId, notAllowedUserId),
                "for getting booking info you must be the item owner or the booker");

        verify(bookingRepository).findById(bookingId);
    }

    @Test
    public void updateStatus_whenUserExists_bookingFound_userIsOwner_statusIsWaiting_saveAndReturnApproved() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        Long bookerId = 2L;
        User booker = User.builder()
                .id(bookerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long itemId = 1L;
        Item item = Item.builder()
                .id(itemId)
                .name("a spoon")
                .description("silver")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        Long bookingId = 1L;
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Booking booking = Booking.builder()
                .id(bookingId)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Boolean approved = true;
        Booking approvedBooking = booking.toBuilder()
                .status(BookingStatus.APPROVED)
                .build();
        BookingResponseDto expectedBooking = BookingMapper.toBookingResponseDto(approvedBooking);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.approveBookingStatus(approved, bookingId, ownerId);

        InOrder inOrder = inOrder(userRepository, bookingRepository);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(bookingRepository).findById(bookingId);
        inOrder.verify(bookingRepository).save(approvedBooking);

        assertEquals(result, expectedBooking);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "a spoon")
                .hasFieldOrPropertyWithValue("item.description", "silver")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.request", null)
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", "Oleg")
                .hasFieldOrPropertyWithValue("booker.email", "Oleg@yandex.ru")
                .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED);
    }

    @Test
    public void updateStatus_whenUserExists_bookingFound_userIsOwner_statusIsWaiting_saveAndReturnRejected() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        Long bookerId = 2L;
        User booker = User.builder()
                .id(bookerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long itemId = 1L;
        Item item = Item.builder()
                .id(itemId)
                .name("a spoon")
                .description("silver")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        Long bookingId = 1L;
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = start.plusWeeks(1);

        Booking booking = Booking.builder()
                .id(bookingId)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Boolean approved = false;
        Booking rejectedBooking = booking.toBuilder()
                .status(BookingStatus.REJECTED)
                .build();
        BookingResponseDto expectedBooking = BookingMapper.toBookingResponseDto(rejectedBooking);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.approveBookingStatus(approved, bookingId, ownerId);

        InOrder inOrder = inOrder(userRepository, bookingRepository);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(bookingRepository).findById(bookingId);
        inOrder.verify(bookingRepository).save(rejectedBooking);

        assertEquals(result, expectedBooking);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", bookingId)
                .hasFieldOrPropertyWithValue("start", start)
                .hasFieldOrPropertyWithValue("end", end)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("item.id", itemId)
                .hasFieldOrPropertyWithValue("item.name", "a spoon")
                .hasFieldOrPropertyWithValue("item.description", "silver")
                .hasFieldOrPropertyWithValue("item.available", true)
                .hasFieldOrPropertyWithValue("item.request", null)
                .hasFieldOrPropertyWithValue("booker", booker)
                .hasFieldOrPropertyWithValue("booker.id", bookerId)
                .hasFieldOrPropertyWithValue("booker.name", "Oleg")
                .hasFieldOrPropertyWithValue("booker.email", "Oleg@yandex.ru")
                .hasFieldOrPropertyWithValue("status", BookingStatus.REJECTED);

    }

    @Test
    public void updateStatus_whenUserDoesNotExists_thenThrowsUserNotExistException_NotInvokeAnyMore() {

        Long userId = 1L;

        Long bookingId = 1L;

        Boolean approved = false;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class,
                () -> bookingService.approveBookingStatus(approved, bookingId, userId),
                String.format("there is no user with id: %s", userId));

        verify(userRepository).findById(userId);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    public void updateStatus_whenUserExists_BookingDoesNotExist_thenThrowsBookingIsNotExist_NotInvokeAnyMore() {

        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .build();

        Long bookingId = 1L;

        Boolean approved = false;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        assertThrows(BookingIsNotExistException.class,
                () -> bookingService.approveBookingStatus(approved, bookingId, userId),
                String.format("there is no booking with id: %s", bookingId));

        verify(userRepository).findById(userId);
        verify(bookingRepository).findById(bookingId);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    public void updateStatus_whenUserExists_BookingExists_UserIsNotOwner_thenThrowsAccessIsDeniedException() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .build();

        Long bookerId = 2L;
        User booker = User.builder()
                .id(bookerId)
                .build();

        Long itemId = 1L;
        Item item = Item.builder()
                .id(itemId)
                .owner(owner)
                .build();

        Long bookingId = 1L;
        Booking booking = Booking.builder()
                .id(bookingId)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Boolean approved = true;

        when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(booker));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of((booking)));

        assertThrows(AccessIsDeniedException.class,
                () -> bookingService.approveBookingStatus(approved, bookingId, bookerId),
                "to set booking status you must be the owner!!!");

        verify(userRepository).findById(bookerId);
        verify(bookingRepository).findById(bookingId);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    public void updateStatus_whenUserExists_BookingExists_UserIsOwner_StatusIsNotWaiting_thenThrowsItemNotAvailable() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .build();

        Long bookerId = 2L;
        User booker = User.builder()
                .id(bookerId)
                .build();

        Long itemId = 1L;
        Item item = Item.builder()
                .id(itemId)
                .owner(owner)
                .build();

        Long bookingId = 1L;
        Booking booking = Booking.builder()
                .id(bookingId)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        Boolean approved = false;

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of((booking)));

        assertThrows(ItemNotAvailableException.class,
                () -> bookingService.approveBookingStatus(approved, bookingId, ownerId),
                String.format("you can not change previous booking status: %s", booking.getStatus()));

        verify(userRepository).findById(ownerId);
        verify(bookingRepository).findById(bookingId);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    public void getListByOwner_whenUserExists_whenStateIsAll_invokeAppropriateMethod_andReturnResult() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        String state = "ALL";

        int from = 1;
        int size = 1;
        Pageable page = PageRequest.of(from / size, size);

        Long booking1Id = 1L;
        Booking booking1 = Booking.builder()
                .id(booking1Id)
                .status(BookingStatus.APPROVED)
                .build();
        Long booking2Id = 2L;
        Booking booking2 = Booking.builder()
                .id(booking2Id)
                .status(BookingStatus.REJECTED)
                .build();
        List<Booking> bookings = List.of(booking1, booking2);

        List<BookingResponseDto> expectedList = BookingMapper.toBookingResponseDtoList(bookings);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(ownerId, page))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(ownerId, state, from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository);

        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(bookingRepository).findAllByItemOwnerIdOrderByStartDesc(ownerId, page);

        assertEquals(result, expectedList);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), booking1Id);
        assertEquals(result.get(0).getStatus(), BookingStatus.APPROVED);
        assertEquals(result.get(1).getId(), booking2Id);
        assertEquals(result.get(1).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void getListByOwner_whenUserExists_whenStateIsCurrent_invokeAppropriateMethod_andReturnResult() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        String state = "CURRENT";

        int from = 10;
        int size = 2;
        Pageable page = PageRequest.of(from / size, size);

        Long booking1Id = 1L;
        Booking booking1 = Booking.builder()
                .id(booking1Id)
                .status(BookingStatus.APPROVED)
                .build();
        Long booking2Id = 2L;
        Booking booking2 = Booking.builder()
                .id(booking2Id)
                .status(BookingStatus.REJECTED)
                .build();
        List<Booking> bookings = List.of(booking1, booking2);

        List<BookingResponseDto> expectedList = BookingMapper.toBookingResponseDtoList(bookings);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(eq(ownerId),
                any(), any(), eq(page))).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(ownerId, state, from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository);

        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(bookingRepository).findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                eq(ownerId), any(), any(), eq(page));

        assertEquals(result, expectedList);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), booking1Id);
        assertEquals(result.get(0).getStatus(), BookingStatus.APPROVED);
        assertEquals(result.get(1).getId(), booking2Id);
        assertEquals(result.get(1).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void getListByOwner_whenUserExists_whenStateIsPast_invokeAppropriateMethod_andReturnResult() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        String state = "PAST";

        int from = 10;
        int size = 10;
        Pageable page = PageRequest.of(from / size, size);

        Long booking1Id = 1L;
        Booking booking1 = Booking.builder()
                .id(booking1Id)
                .status(BookingStatus.APPROVED)
                .build();
        Long booking2Id = 2L;
        Booking booking2 = Booking.builder()
                .id(booking2Id)
                .status(BookingStatus.REJECTED)
                .build();
        List<Booking> bookings = List.of(booking1, booking2);

        List<BookingResponseDto> expectedList = BookingMapper.toBookingResponseDtoList(bookings);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(eq(ownerId),
                any(), eq(page))).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(ownerId, state, from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository);

        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(bookingRepository).findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(
                eq(ownerId), any(), eq(page));

        assertEquals(result, expectedList);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), booking1Id);
        assertEquals(result.get(0).getStatus(), BookingStatus.APPROVED);
        assertEquals(result.get(1).getId(), booking2Id);
        assertEquals(result.get(1).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void getListByOwner_whenUserExists_whenStateIsFuture_invokeAppropriateMethod_andReturnResult() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        String state = "FUTURE";

        int from = 10;
        int size = 10;
        Pageable page = PageRequest.of(from / size, size);

        Long booking1Id = 1L;
        Booking booking1 = Booking.builder()
                .id(booking1Id)
                .status(BookingStatus.APPROVED)
                .build();
        Long booking2Id = 2L;
        Booking booking2 = Booking.builder()
                .id(booking2Id)
                .status(BookingStatus.REJECTED)
                .build();
        List<Booking> bookings = List.of(booking1, booking2);

        List<BookingResponseDto> expectedList = BookingMapper.toBookingResponseDtoList(bookings);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(eq(ownerId),
                any(), eq(page))).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(ownerId, state, from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository);

        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(bookingRepository).findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(
                eq(ownerId), any(), eq(page));
        assertEquals(result, expectedList);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), booking1Id);
        assertEquals(result.get(0).getStatus(), BookingStatus.APPROVED);
        assertEquals(result.get(1).getId(), booking2Id);
        assertEquals(result.get(1).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void getListByOwner_whenUserExists_whenStateIsRejected_invokeAppropriateMethod_andReturnResult() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        String state = "REJECTED";

        List<BookingStatus> notApprovedStatus = List.of(BookingStatus.REJECTED, BookingStatus.CANCELED);

        int from = 10;
        int size = 10;
        Pageable page = PageRequest.of(from / size, size);

        Long booking1Id = 1L;
        Booking booking1 = Booking.builder()
                .id(booking1Id)
                .status(BookingStatus.REJECTED)
                .build();
        Long booking2Id = 2L;
        Booking booking2 = Booking.builder()
                .id(booking2Id)
                .status(BookingStatus.REJECTED)
                .build();
        List<Booking> bookings = List.of(booking1, booking2);

        List<BookingResponseDto> expectedList = BookingMapper.toBookingResponseDtoList(bookings);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStatusInOrderByStartDesc(ownerId, notApprovedStatus, page))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(ownerId, state, from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository);

        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(bookingRepository)
                .findAllByItemOwnerIdAndStatusInOrderByStartDesc(ownerId, notApprovedStatus, page);

        assertEquals(result, expectedList);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), booking1Id);
        assertEquals(result.get(0).getStatus(), BookingStatus.REJECTED);
        assertEquals(result.get(1).getId(), booking2Id);
        assertEquals(result.get(1).getStatus(), BookingStatus.REJECTED);

    }

    @Test
    public void getListByOwner_whenUserExists_whenStateIsWaiting_invokeAppropriateMethod_andReturnResult() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        String state = "WAITING";

        BookingStatus status = BookingStatus.WAITING;

        int from = 10;
        int size = 10;
        Pageable page = PageRequest.of(from / size, size);

        Long booking1Id = 1L;
        Booking booking1 = Booking.builder()
                .id(booking1Id)
                .status(BookingStatus.WAITING)
                .build();
        Long booking2Id = 2L;
        Booking booking2 = Booking.builder()
                .id(booking2Id)
                .status(BookingStatus.WAITING)
                .build();
        List<Booking> bookings = List.of(booking1, booking2);

        List<BookingResponseDto> expectedList = BookingMapper.toBookingResponseDtoList(bookings);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, status, page))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(ownerId, state, from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository);

        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(bookingRepository)
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, status, page);

        assertEquals(result, expectedList);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), booking1Id);
        assertEquals(result.get(0).getStatus(), BookingStatus.WAITING);
        assertEquals(result.get(1).getId(), booking2Id);
        assertEquals(result.get(1).getStatus(), BookingStatus.WAITING);
    }

    @Test
    public void getListByOwner_whenUserDoesNotExist_thenThrowsUserNotExistException_NotInvokeAnyMore() {

        Long ownerId = 1L;

        String state = "WAITING";

        int from = 0;
        int size = 10;

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class,
                () -> bookingService.getBookingsByOwner(ownerId, state, from, size),
                String.format("there is no user with id: %s", ownerId));

        verify(userRepository).findById(ownerId);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    public void getListByOwner_whenUserExist_butStateIsNotValid_thenThrowUnsupportedStatus_NotInvokeAnyMore() {

        Long ownerId = 1L;
        User owner = User.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        String state = "INVALID";

        int from = 0;
        int size = 10;

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));

        assertThrows(UnsupportedStatusException.class,
                () -> bookingService.getBookingsByOwner(ownerId, state, from, size),
                "Unknown state: UNSUPPORTED_STATUS");

        verify(userRepository).findById(ownerId);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    public void getListByBooker_whenUserExists_whenStateIsAll_invokeAppropriateMethod_andReturnResult() {

        Long bookerId = 1L;
        User booker = User.builder()
                .id(bookerId)
                .name("Peter")
                .build();

        String state = "ALL";

        int from = 10;
        int size = 10;
        Pageable page = PageRequest.of(from / size, size);

        Long booking1Id = 1L;
        Booking booking1 = Booking.builder()
                .id(booking1Id)
                .status(BookingStatus.APPROVED)
                .build();
        Long booking2Id = 2L;
        Booking booking2 = Booking.builder()
                .id(booking2Id)
                .status(BookingStatus.REJECTED)
                .build();
        List<Booking> bookings = List.of(booking1, booking2);

        List<BookingResponseDto> expectedList = BookingMapper.toBookingResponseDtoList(bookings);

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId, page)).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository);

        inOrder.verify(userRepository).findById(bookerId);
        inOrder.verify(bookingRepository).findAllByBookerIdOrderByStartDesc(bookerId, page);

        assertEquals(result, expectedList);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), booking1Id);
        assertEquals(result.get(0).getStatus(), BookingStatus.APPROVED);
        assertEquals(result.get(1).getId(), booking2Id);
        assertEquals(result.get(1).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void getListByBooker_whenUserExists_whenStateIsCurrent_invokeAppropriateMethod_andReturnResult() {

        Long bookerId = 1L;
        User booker = User.builder()
                .id(bookerId)
                .build();

        String state = "CURRENT";

        int from = 10;
        int size = 10;
        Pageable page = PageRequest.of(from / size, size);

        Long booking1Id = 1L;
        Booking booking1 = Booking.builder()
                .id(booking1Id)
                .status(BookingStatus.APPROVED)
                .build();
        Long booking2Id = 2L;
        Booking booking2 = Booking.builder()
                .id(booking2Id)
                .status(BookingStatus.REJECTED)
                .build();
        List<Booking> bookings = List.of(booking1, booking2);

        List<BookingResponseDto> expectedList = BookingMapper.toBookingResponseDtoList(bookings);

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(eq(bookerId),
                any(), any(), eq(page))).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository);

        inOrder.verify(userRepository).findById(bookerId);
        inOrder.verify(bookingRepository).findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                eq(bookerId), any(), any(), eq(page));

        assertEquals(result, expectedList);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), booking1Id);
        assertEquals(result.get(0).getStatus(), BookingStatus.APPROVED);
        assertEquals(result.get(1).getId(), booking2Id);
        assertEquals(result.get(1).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void getListByBooker_whenUserExists_whenStateIsPast_invokeAppropriateMethod_andReturnResult() {

        Long bookerId = 1L;
        User booker = User.builder()
                .id(bookerId)
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        String state = "PAST";

        int from = 10;
        int size = 10;
        Pageable page = PageRequest.of(from / size, size);

        Long booking1Id = 1L;
        Booking booking1 = Booking.builder()
                .id(booking1Id)
                .status(BookingStatus.APPROVED)
                .build();
        Long booking2Id = 2L;
        Booking booking2 = Booking.builder()
                .id(booking2Id)
                .status(BookingStatus.REJECTED)
                .build();
        List<Booking> bookings = List.of(booking1, booking2);

        List<BookingResponseDto> expectedList = BookingMapper.toBookingResponseDtoList(bookings);

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(eq(bookerId),
                any(), eq(page))).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository);

        inOrder.verify(userRepository).findById(bookerId);
        inOrder.verify(bookingRepository).findAllByBookerIdAndEndIsBeforeOrderByStartDesc(
                eq(bookerId), any(), eq(page));

        assertEquals(result, expectedList);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), booking1Id);
        assertEquals(result.get(0).getStatus(), BookingStatus.APPROVED);
        assertEquals(result.get(1).getId(), booking2Id);
        assertEquals(result.get(1).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void getListByBooker_whenUserExists_whenStateIsFuture_invokeAppropriateMethod_andReturnResult() {

        Long bookerId = 1L;
        User booker = User.builder()
                .id(bookerId)
                .build();

        String state = "FUTURE";

        int from = 10;
        int size = 10;
        Pageable page = PageRequest.of(from / size, size);

        Long booking1Id = 1L;
        Booking booking1 = Booking.builder()
                .id(booking1Id)
                .status(BookingStatus.APPROVED)
                .build();
        Long booking2Id = 2L;
        Booking booking2 = Booking.builder()
                .id(booking2Id)
                .status(BookingStatus.REJECTED)
                .build();
        List<Booking> bookings = List.of(booking1, booking2);

        List<BookingResponseDto> expectedList = BookingMapper.toBookingResponseDtoList(bookings);

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStartIsAfterOrderByStartDesc(eq(bookerId),
                any(), eq(page))).thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository);

        inOrder.verify(userRepository).findById(bookerId);
        inOrder.verify(bookingRepository).findAllByBookerIdAndStartIsAfterOrderByStartDesc(
                eq(bookerId), any(), eq(page));
        assertEquals(result, expectedList);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), booking1Id);
        assertEquals(result.get(0).getStatus(), BookingStatus.APPROVED);
        assertEquals(result.get(1).getId(), booking2Id);
        assertEquals(result.get(1).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void getListByBooker_whenUserExists_whenStateIsRejected_invokeAppropriateMethod_andReturnResult() {

        Long bookerId = 1L;
        User booker = User.builder()
                .id(bookerId)
                .name("Igor")
                .build();

        String state = "REJECTED";

        List<BookingStatus> notApprovedStatus = List.of(BookingStatus.REJECTED, BookingStatus.CANCELED);

        int from = 10;
        int size = 10;
        Pageable page = PageRequest.of(from / size, size);

        Long booking1Id = 1L;
        Booking booking1 = Booking.builder()
                .id(booking1Id)
                .status(BookingStatus.REJECTED)
                .build();
        Long booking2Id = 2L;
        Booking booking2 = Booking.builder()
                .id(booking2Id)
                .status(BookingStatus.REJECTED)
                .build();
        List<Booking> bookings = List.of(booking1, booking2);

        List<BookingResponseDto> expectedList = BookingMapper.toBookingResponseDtoList(bookings);

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusInOrderByStartDesc(bookerId, notApprovedStatus, page))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository);

        inOrder.verify(userRepository).findById(bookerId);
        inOrder.verify(bookingRepository)
                .findAllByBookerIdAndStatusInOrderByStartDesc(bookerId, notApprovedStatus, page);
        assertEquals(result, expectedList);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), booking1Id);
        assertEquals(result.get(0).getStatus(), BookingStatus.REJECTED);
        assertEquals(result.get(1).getId(), booking2Id);
        assertEquals(result.get(1).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    public void getListByBooker_whenUserExists_whenStateIsWaiting_invokeAppropriateMethod_andReturnResult() {

        Long bookerId = 1L;
        User booker = User.builder()
                .id(bookerId)
                .name("Mary")
                .build();

        String state = "WAITING";

        BookingStatus status = BookingStatus.WAITING;

        int from = 10;
        int size = 10;
        Pageable page = PageRequest.of(from / size, size);

        Long booking1Id = 1L;
        Booking booking1 = Booking.builder()
                .id(booking1Id)
                .status(BookingStatus.WAITING)
                .build();
        Long booking2Id = 2L;
        Booking booking2 = Booking.builder()
                .id(booking2Id)
                .status(BookingStatus.WAITING)
                .build();
        List<Booking> bookings = List.of(booking1, booking2);

        List<BookingResponseDto> expectedList = BookingMapper.toBookingResponseDtoList(bookings);

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, status, page))
                .thenReturn(bookings);

        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, state, from, size);

        InOrder inOrder = inOrder(userRepository, bookingRepository);

        inOrder.verify(userRepository).findById(bookerId);
        inOrder.verify(bookingRepository)
                .findAllByBookerIdAndStatusOrderByStartDesc(bookerId, status, page);

        assertEquals(result, expectedList);

        assertThat(result).asList()
                .hasSize(2)
                .hasOnlyElementsOfType(BookingResponseDto.class);

        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking1)));
        MatcherAssert.assertThat(result, hasItem(BookingMapper.toBookingResponseDto(booking2)));

        assertEquals(result.get(0).getId(), booking1Id);
        assertEquals(result.get(0).getStatus(), BookingStatus.WAITING);
        assertEquals(result.get(1).getId(), booking2Id);
        assertEquals(result.get(1).getStatus(), BookingStatus.WAITING);

    }

    @Test
    public void getListByBooker_whenUserDoesNotExist_thenThrowsUserNotExistException_NotInvokeAnyMore() {

        Long bookerId = 1L;

        String state = "WAITING";

        int from = 0;
        int size = 10;

        when(userRepository.findById(bookerId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class,
                () -> bookingService.getBookingsByBooker(bookerId, state, from, size),
                String.format("there is no user with id: %s", bookerId));

        verify(userRepository).findById(bookerId);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    public void getListByBooker_whenUserExist_butStateIsNotValid_thenThrowsUnsupportedStatus_NotInvokeAnyMore() {

        Long bookerId = 1L;
        User booker = User.builder()
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        String state = "INVALID";

        int from = 0;
        int size = 10;

        when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(booker));

        assertThrows(UnsupportedStatusException.class,
                () -> bookingService.getBookingsByBooker(bookerId, state, from, size),
                "Unknown state: UNSUPPORTED_STATUS");

        verify(userRepository).findById(bookerId);
        verifyNoInteractions(bookingRepository);
    }

}
