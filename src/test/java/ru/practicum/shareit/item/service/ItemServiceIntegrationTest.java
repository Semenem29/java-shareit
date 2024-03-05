package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemResponseDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingJPAService;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemServiceIntegrationTest {
    @Autowired
    UserJPAService userService;
    @Autowired
    ItemJPAService itemService;
    @Autowired
    BookingJPAService bookingService;
    @Autowired
    ItemRequestJPAService itemRequestService;

    @Test
    public void shouldCreateItemWithRequest() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Jorge")
                .email("Jorge@yandex.ru")
                .build();
        userService.createUser(requesterDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent knife")
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();

        ItemDto result = itemService.createItem(itemDto, ownerId);

        assertEquals(result.getId(), itemId);
        assertEquals(result.getAvailable(), true);
        assertEquals(result.getName(), "knife");
        assertEquals(result.getDescription(), "new");
        assertEquals(result.getRequestId(), requestId);
    }

    @Test
    public void shouldCreateItemWithoutRequest() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.createUser(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .build();

        Item item = ItemMapper.toItem(itemDto, owner, null);
        ItemDto itemDtoWithoutRequest = ItemMapper.toItemDto(item);
        ItemDto expectedItemDto = itemDtoWithoutRequest.toBuilder().id(itemId).build();

        ItemDto result = itemService.createItem(itemDtoWithoutRequest, ownerId);

        assertEquals(result, expectedItemDto);
        assertEquals(result.getAvailable(), true);
        assertEquals(result.getName(), "knife");
        assertEquals(result.getDescription(), "new");
        assertNull(result.getRequestId());
    }

    @Test
    public void shouldFailCreateIfUserNotFound() {

        Long ownerId = 1L;

        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .build();

        assertThrows(UserNotExistException.class,
                () -> itemService.createItem(itemDto, ownerId),
                String.format("there is no user with id: %s", ownerId));
    }

    @Test
    public void shouldFailCreateIfRequestIsNotFoundByRequestId() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long requestId = 1L;

        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .requestId(requestId)
                .available(true)
                .build();

        assertThrows(ItemRequestNotExistException.class,
                () -> itemService.createItem(itemDto, ownerId),
                String.format("there is no item request with id: %s", requestId));
    }

    @Test
    public void shouldGetItemByIdWithoutBookingsForNotOwner() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Jorge")
                .email("Jorge@yandex.ru")
                .build();
        userService.createUser(requesterDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent knife")
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        ItemResponseDto result = itemService.getItemById(ownerId, itemId);

        assertEquals(result.getAvailable(), true);
        assertEquals(result.getName(), "knife");
        assertEquals(result.getDescription(), "new");
        assertEquals(result.getRequestId(), requestId);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertEquals(result.getComments(), Collections.emptyList());
    }

    @Test
    public void shouldGetItemByIdWithBookingsForOwner() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Jorge")
                .email("Jorge@yandex.ru")
                .build();
        userService.createUser(requesterDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent knife")
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        Long nextBookingId = 1L;
        BookingDto nextBookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.of(2030, 1, 1, 1, 1, 1))
                .end(LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                .build();
        BookingResponseDto nextBookingOutDto = bookingService.createBooking(nextBookingDto, requesterId);
        bookingService.approveBookingStatus(true, nextBookingId, ownerId);
        Booking nextBooking = BookingMapper.toBooking(nextBookingOutDto);
        BookingItemResponseDto nextBookingItemDto = BookingMapper.toBookingItemResponseDto(nextBooking);

        ItemResponseDto result = itemService.getItemById(ownerId, itemId);

        assertThat(result).hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "knife")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("lastBooking", null)
                .hasFieldOrPropertyWithValue("nextBooking", nextBookingItemDto)
                .hasFieldOrPropertyWithValue("comments", Collections.emptyList());
    }

    @Test
    public void shouldGetByIdForOwnerWithoutBookings() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Jorge")
                .email("Jorge@yandex.ru")
                .build();
        userService.createUser(requesterDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent knife")
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .build();
        itemService.createItem(itemDto, ownerId);

        ItemResponseDto result = itemService.getItemById(ownerId, itemId);

        assertThat(result).hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "knife")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("lastBooking", null)
                .hasFieldOrPropertyWithValue("nextBooking", null)
                .hasFieldOrPropertyWithValue("comments", Collections.emptyList());
    }

    @Test
    public void shouldThrowExceptionIfItemNotFoundById() {

        Long ownerId = 1L;

        Long itemId = 1L;

        assertThrows(ItemNotExistException.class,
                () -> itemService.getItemById(itemId, ownerId),
                String.format("there is no item with id: %s", itemId));
    }

    @Test
    public void shouldUpdateAllFieldsExcludingId() {

        ItemDto itemDtoToUpdate = ItemDto.builder()
                .id(44L)
                .name("NewName")
                .description("NewDescription")
                .available(false)
                .build();

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Jorge")
                .email("Jorge@yandex.ru")
                .build();
        userService.createUser(requesterDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent knife")
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        ItemDto result = itemService.updateItem(itemDtoToUpdate, ownerId, itemId);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "NewName")
                .hasFieldOrPropertyWithValue("description", "NewDescription")
                .hasFieldOrPropertyWithValue("available", false)
                .hasFieldOrPropertyWithValue("requestId", requestId);
    }

    @Test
    public void shouldUpdateOnlyName() {

        ItemDto itemDtoToUpdate = ItemDto.builder()
                .id(44L)
                .name("NewName")
                .build();

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Jorge")
                .email("Jorge@yandex.ru")
                .build();
        userService.createUser(requesterDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent knife")
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        ItemDto result = itemService.updateItem(itemDtoToUpdate, ownerId, itemId);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "NewName")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("requestId", requestId);
    }

    @Test
    public void shouldUpdateOnlyDescription() {

        ItemDto itemDtoToUpdate = ItemDto.builder()
                .description("newDescription")
                .build();

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Jorge")
                .email("Jorge@yandex.ru")
                .build();
        userService.createUser(requesterDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent knife")
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        ItemDto result = itemService.updateItem(itemDtoToUpdate, ownerId, itemId);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "knife")
                .hasFieldOrPropertyWithValue("description", "newDescription")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("requestId", requestId);
    }

    @Test
    public void shouldUpdateOnlyAvailable() {

        ItemDto itemDtoToUpdate = ItemDto.builder()
                .id(44L)
                .available(false)
                .build();

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Jorge")
                .email("Jorge@yandex.ru")
                .build();
        userService.createUser(requesterDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent knife")
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();
        itemService.createItem(itemDto, ownerId);

        ItemDto result = itemService.updateItem(itemDtoToUpdate, ownerId, itemId);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "knife")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", false)
                .hasFieldOrPropertyWithValue("requestId", requestId);
    }

    @Test
    public void shouldReturnNonUpdatedItem() {

        ItemDto invalidItemDtoToUpdate = ItemDto.builder()
                .name("")
                .description("")
                .available(null)
                .build();

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Jorge")
                .email("Jorge@yandex.ru")
                .build();
        userService.createUser(requesterDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent knife")
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .build();
        itemService.createItem(itemDto, ownerId);

        ItemDto result = itemService.updateItem(invalidItemDtoToUpdate, ownerId, itemId);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "knife")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("requestId", null);
    }

    @Test
    public void shouldFailUpdateIfUserDoesNotExist() {

        Long ownerId = 1L;

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .build();

        assertThrows(UserNotExistException.class,
                () -> itemService.updateItem(itemDto, itemId, ownerId),
                String.format("there is no user with id: %s", ownerId));
    }

    @Test
    public void shouldFailUpdateIfItemDoesNotExist() {

        Long ownerId = 1L;
        UserDto owner = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(owner);

        Long itemId = 1L;

        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .build();

        assertThrows(ItemNotExistException.class,
                () -> itemService.updateItem(itemDto, ownerId, itemId),
                String.format("there is no item with id: %s", itemId));
    }

    @Test
    public void shouldFailUpdateIfUserIsNotOwner() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.createUser(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        Long notOwnerId = 2L;
        UserDto notOwnerDto = UserDto.builder()
                .id(notOwnerId)
                .name("Jorge")
                .email("Jorge@yandex.ru")
                .build();
        userService.createUser(notOwnerDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .build();
        ItemDto savedItemDto = itemService.createItem(itemDto, ownerId);

        assertThrows(AttempToUpdateNotYourItemException.class,
                () -> itemService.updateItem(itemDto, notOwnerId, itemId),
                "you are not allow to edit the item, because you are not the owner");
    }

    @Test
    public void shouldGetListOfItemsByUser() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Jorge")
                .email("Jorge@yandex.ru")
                .build();
        userService.createUser(requesterDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent knife")
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto);

        Long item1Id = 1L;
        ItemDto item1Dto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .build();
        itemService.createItem(item1Dto, ownerId);

        Long item2Id = 2L;
        ItemDto itemDto2 = ItemDto.builder()
                .name("pram")
                .description("old")
                .available(true)
                .build();
        itemService.createItem(itemDto2, ownerId);

        Long nextBookingId = 1L;
        BookingDto nextBookingDto = BookingDto.builder()
                .itemId(item1Id)
                .start(LocalDateTime.of(2030, 1, 1, 1, 1, 1))
                .end(LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                .build();
        BookingResponseDto nextBookingOutDto = bookingService.createBooking(nextBookingDto, requesterId);
        bookingService.approveBookingStatus(true, nextBookingId, ownerId);
        Booking nextBooking = BookingMapper.toBooking(nextBookingOutDto);
        BookingItemResponseDto nextBookingItemDto = BookingMapper.toBookingItemResponseDto(nextBooking);

        int from = 0;
        int size = 10;

        List<ItemResponseDto> result = itemService.getItemsOfOwner(ownerId, from, size);

        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getId(), item1Id);
        assertEquals(result.get(0).getName(), "knife");
        assertEquals(result.get(0).getDescription(), "new");
        assertEquals(result.get(0).getAvailable(), true);
        assertNull(result.get(0).getLastBooking());
        assertEquals(result.get(0).getNextBooking(), nextBookingItemDto);
        assertEquals(result.get(0).getNextBooking().getId(), nextBookingId);
        assertEquals(result.get(0).getComments(), Collections.emptyList());
        assertEquals(result.get(1).getId(), item2Id);
        assertEquals(result.get(1).getName(), "pram");
        assertEquals(result.get(1).getDescription(), "old");
        assertEquals(result.get(1).getAvailable(), true);
        assertNull(result.get(1).getLastBooking());
        assertNull(result.get(1).getNextBooking());
        assertEquals(result.get(1).getComments().size(), 0);
    }

    @Test
    public void shouldGetEmptyListOfItems() {
        int from = 0;
        int size = 10;
        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        List<ItemResponseDto> result = itemService.getItemsOfOwner(ownerId, from, size);

        assertEquals(result, Collections.emptyList());
        assertEquals(result.size(), 0);
    }

    @Test
    public void shouldSearchItemsBySubstring() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.createUser(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Jorge")
                .email("Jorge@yandex.ru")
                .build();
        UserDto savedRequesterDto = userService.createUser(requesterDto);
        User requester = UserMapper.toUser(savedRequesterDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent knife")
                .build();
        ItemRequestResponseDto itemRequestOutDto = itemRequestService.createRequest(requesterId, itemRequestDto);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestOutDto, requester);

        Long item1Id = 1L;
        ItemDto item1Dto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .build();
        ItemDto savedItemDto1 = itemService.createItem(item1Dto, ownerId);
        Item item1 = ItemMapper.toItem(savedItemDto1, owner, itemRequest);
        ItemDto itemDto2 = ItemDto.builder()
                .name("pram")
                .description("old")
                .available(true)
                .build();
        itemService.createItem(itemDto2, ownerId);

        Long nextBookingId = 1L;
        BookingDto nextBookingDto = BookingDto.builder()
                .itemId(item1Id)
                .start(LocalDateTime.of(2030, 1, 1, 1, 1, 1))
                .end(LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                .build();
        BookingResponseDto nextBookingOutDto = bookingService.createBooking(nextBookingDto, requesterId);
        bookingService.approveBookingStatus(true, nextBookingId, ownerId);
        Booking nextBooking = BookingMapper.toBooking(nextBookingOutDto);
        BookingItemResponseDto nextBookingItemDto = BookingMapper.toBookingItemResponseDto(nextBooking);

        ItemResponseDto itemOutDto1 = ItemMapper.toItemResponseDto(item1,
                null, nextBookingItemDto, Collections.emptyList());

        List<ItemResponseDto> expectedItemsListBySearch = List.of(itemOutDto1);

        int from = 0;
        int size = 10;

        List<ItemResponseDto> result = itemService.findItemsByText("Ife", from, size);

        assertEquals(expectedItemsListBySearch, result);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId(), item1Id);
        assertEquals(result.get(0).getName(), "knife");
        assertEquals(result.get(0).getDescription(), "new");
        assertEquals(result.get(0).getAvailable(), true);
        assertNull(result.get(0).getLastBooking());
        assertEquals(result.get(0).getNextBooking(), nextBookingItemDto);
        assertEquals(result.get(0).getNextBooking().getId(), nextBookingId);
        assertEquals(result.get(0).getComments(), Collections.emptyList());
    }

    @Test
    public void shouldGetEmptyListOfItemsBySearch() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long requesterId = 2L;
        UserDto requesterDto = UserDto.builder()
                .name("Jorge")
                .email("Jorge@yandex.ru")
                .build();
        userService.createUser(requesterDto);

        Long requestId = 1L;
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("I would like to rent knife")
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto);

        ItemDto item1Dto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .build();
        itemService.createItem(item1Dto, ownerId);

        ItemDto itemDto2 = ItemDto.builder()
                .name("pram")
                .description("old")
                .available(true)
                .build();
        itemService.createItem(itemDto2, ownerId);
        int from = 0;
        int size = 10;

        List<ItemResponseDto> result = itemService.findItemsByText("text", from, size);

        assertEquals(result.size(), 0);
    }

    @Test
    public void shouldFailAddCommentWhenItemDoesNotExist() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long itemId = 1L;

        CommentRequestDto commentDto = CommentRequestDto.builder()
                .itemId(itemId)
                .text("bad knife!")
                .build();

        assertThrows(ItemNotExistException.class,
                () -> itemService.addComment(ownerId, itemId, commentDto),
                String.format("there is no item with id: %s", itemId));
    }

    @Test
    public void shouldFailAddCommentWhenUserDoesNotExist() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        userService.createUser(ownerDto);

        Long itemId = 1L;
        ItemDto item1Dto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .build();
        itemService.createItem(item1Dto, ownerId);

        CommentRequestDto commentDto = CommentRequestDto.builder()
                .itemId(itemId)
                .text("bad knife!")
                .build();

        Long nonExistedId = -1L;

        assertThrows(UserNotExistException.class,
                () -> itemService.addComment(nonExistedId, itemId, commentDto),
                String.format("there is no user with id:%s", ownerId));
    }

    @Test
    public void shouldFailAddCommentWhenUserIsOwner() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.createUser(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .build();
        itemService.createItem(itemDto, ownerId);

        CommentRequestDto commentDto = CommentRequestDto.builder()
                .itemId(itemId)
                .authorName(owner.getName())
                .text("bad knife!")
                .build();

        assertThrows(ValidationException.class,
                () -> itemService.addComment(ownerId, itemId, commentDto),
                "it's forbidden to comment your own item");
    }

    @Test
    public void shouldFailAddCommentWhenUserHasNotCurrentOrPastBookings() {

        Long ownerId = 1L;
        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();
        UserDto savedOwnerDto = userService.createUser(ownerDto);
        User owner = UserMapper.toUser(savedOwnerDto);

        Long itemId = 1L;
        ItemDto item1Dto = ItemDto.builder()
                .name("knife")
                .description("new")
                .available(true)
                .build();
        itemService.createItem(item1Dto, ownerId);

        CommentRequestDto commentDto = CommentRequestDto.builder()
                .itemId(itemId)
                .authorName(owner.getName())
                .text("bad knife!")
                .build();

        Long notOwnerId = 2L;
        UserDto notOwnerDto = UserDto.builder()
                .name("Jorge")
                .email("Jorge@yandex.ru")
                .build();
        userService.createUser(notOwnerDto);

        assertThrows(ValidationException.class,
                () -> itemService.addComment(notOwnerId, itemId, commentDto),
                "it's forbidden to comment item you've never booked");
    }
}
