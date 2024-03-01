package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingItemResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.store.BookingJPARepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentJPARepository;
import ru.practicum.shareit.item.storage.ItemJPARepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.store.ItemRequestJPARepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJPARepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
    @InjectMocks
    private ItemJPAServiceImpl itemService;
    @Mock
    private UserJPARepository userRepository;
    @Mock
    private ItemJPARepository itemRepository;
    @Mock
    private BookingJPARepository bookingRepository;
    @Mock
    private ItemRequestJPARepository itemRequestRepository;
    @Mock
    private CommentJPARepository commentRepository;

    @Test
    public void create_whenUserExists_whenRequestIdIsNull_InvokeSave_AndReturnSavedItem() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();

        Long itemId = 1L;
        Item itemWithoutRequest = ItemMapper.toItem(itemDto, owner, null);
        Item savedItemWithoutRequest = itemWithoutRequest.toBuilder()
                .id(itemId)
                .build();
        ItemDto expectedItem = ItemMapper.toItemDto(savedItemWithoutRequest);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.save(itemWithoutRequest)).thenReturn(savedItemWithoutRequest);

        ItemDto result = itemService.createItem(itemDto, ownerId);

        assertEquals(expectedItem, result);

        verify(userRepository).findById(ownerId);
        verify(itemRepository).save(itemWithoutRequest);
    }

    @Test
    public void create_whenUserExists_whenRequestExistsByRequestId_InvokeSave_AndReturnSavedItem() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent a spoon")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .requestId(requestId)
                .build();

        Long itemId = 1L;
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder().id(itemId).build();
        ItemDto expectedItem = ItemMapper.toItemDto(savedItem);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(item)).thenReturn(savedItem);

        ItemDto result = itemService.createItem(itemDto, ownerId);

        InOrder inOrder = inOrder(userRepository, itemRequestRepository, itemRepository);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(itemRequestRepository).findById(requestId);
        inOrder.verify(itemRepository).save(item);
        inOrder.verifyNoMoreInteractions();

        assertEquals(result, expectedItem);
    }

    @Test
    public void create_whenUserNotExist_thenThrowUserNotExistException_NotInvokeAnyMore() {

        Long ownerId = 1L;

        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class,
                () -> itemService.createItem(itemDto, ownerId),
                String.format("there is no user with id:%s", ownerId));

        verify(userRepository).findById(ownerId);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    public void create_whenUserExists_RequestIdIsNotNullButNotFound_thenThrowItemRequestNotExist_NotInvokeAnyMore() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long requestId = 1L;

        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .requestId(requestId)
                .available(true)
                .build();

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.empty());

        assertThrows(ItemRequestNotExistException.class,
                () -> itemService.createItem(itemDto, ownerId),
                String.format("there is no item request with id: %s", requestId));

        InOrder inOrder = inOrder(userRepository, itemRequestRepository);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(itemRequestRepository).findById(requestId);
    }

    @Test
    public void getById_whenUserExists_whenUserIsNotOwner_doesNotGetBookings_addComments_returnConstructedObject() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent a spoon")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder().id(itemId).build();

        Comment comment1 = Comment.builder()
                .item(item)
                .author(notOwner)
                .text("bad a spoon!")
                .created(LocalDateTime.now().minusWeeks(1))
                .build();
        Comment comment2 = Comment.builder()
                .item(item)
                .author(notOwner)
                .text("I change my mind: good a spoon!")
                .created(LocalDateTime.now())
                .build();
        List<Comment> comments = List.of(comment1, comment2);
        List<CommentResponseDto> commentsOut = CommentMapper.toCommentResponseDtoList(comments);

        ItemResponseDto expectedItemForNotOwner = ItemMapper
                .toItemResponseDto(savedItem, null, null, commentsOut);

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(savedItem));
        when(commentRepository.findAllByItemId(itemId))
                .thenReturn(comments);

        ItemResponseDto result = itemService.getItemById(notOwnerId, itemId);

        InOrder inOrder = inOrder(itemRepository, commentRepository);
        inOrder.verify(itemRepository).findById(itemId);
        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(eq(itemId),
                        eq(BookingStatus.APPROVED), any(), any());
        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(eq(itemId),
                        eq(BookingStatus.APPROVED), any(), any());
        inOrder.verify(commentRepository).findAllByItemId(itemId);

        assertEquals(result, expectedItemForNotOwner);
        assertThat(result).hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "a spoon")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("lastBooking", null)
                .hasFieldOrPropertyWithValue("nextBooking", null)
                .hasFieldOrPropertyWithValue("comments", commentsOut);
    }

    @Test
    public void getById_whenUserExists_whenUserIsOwner_getExistingBookingsInfo_addComments_returnConstructedObject() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent a spoon")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder().id(itemId).build();

        Long lastBookingId = 1L;
        Booking lastBooking = Booking.builder()
                .id(lastBookingId)
                .booker(notOwner)
                .build();
        Long nextBookingId = 2L;
        Booking nextBooking = Booking.builder()
                .id(nextBookingId)
                .booker(notOwner)
                .build();
        BookingItemResponseDto lastBookingDto = BookingMapper.toBookingItemResponseDto(lastBooking);
        BookingItemResponseDto nextBookingDto = BookingMapper.toBookingItemResponseDto(nextBooking);

        Comment comment1 = Comment.builder()
                .item(item)
                .author(notOwner)
                .text("bad a spoon!")
                .created(LocalDateTime.now().minusWeeks(1))
                .build();
        Comment comment2 = Comment.builder()
                .item(item)
                .author(notOwner)
                .text("I change my mind: good a spoon!")
                .created(LocalDateTime.now())
                .build();
        List<Comment> comments = List.of(comment1, comment2);
        List<CommentResponseDto> commentsOut = CommentMapper.toCommentResponseDtoList(comments);

        ItemResponseDto expectedItemForOwner =
                ItemMapper.toItemResponseDto(savedItem, lastBookingDto, nextBookingDto, commentsOut);

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(savedItem));
        when(bookingRepository.findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(eq(itemId),
                eq(BookingStatus.APPROVED), any(), any()))
                .thenReturn(Optional.of(nextBooking));
        when(bookingRepository.findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(eq(itemId),
                eq(BookingStatus.APPROVED), any(), any()))
                .thenReturn(Optional.of(lastBooking));
        when(commentRepository.findAllByItemId(itemId))
                .thenReturn(comments);

        ItemResponseDto result = itemService.getItemById(ownerId, itemId);

        InOrder inOrder = inOrder(itemRepository, commentRepository);
        inOrder.verify(itemRepository).findById(itemId);
        verify(bookingRepository)
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(),
                        any(), any(), any());
        verify(bookingRepository)
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(),
                        any(), any(), any());
        inOrder.verify(commentRepository).findAllByItemId(itemId);

        assertEquals(result, expectedItemForOwner);
        assertThat(result).hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "a spoon")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("lastBooking", lastBookingDto)
                .hasFieldOrPropertyWithValue("nextBooking", nextBookingDto)
                .hasFieldOrPropertyWithValue("comments", commentsOut);
    }

    @Test
    public void getById_whenUserExists_whenUserIsOwner_getNullBookingsInfo_whenCommentsExist_returnConstructedObject() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent a spoon")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder().id(itemId).build();

        Comment comment1 = Comment.builder()
                .item(item)
                .author(notOwner)
                .text("bad a spoon!")
                .created(LocalDateTime.now().minusWeeks(1))
                .build();
        Comment comment2 = Comment.builder()
                .item(item)
                .author(notOwner)
                .text("I change my mind: good a spoon!")
                .created(LocalDateTime.now())
                .build();
        List<Comment> comments = List.of(comment1, comment2);
        List<CommentResponseDto> commentsOut = CommentMapper.toCommentResponseDtoList(comments);

        ItemResponseDto expectedItemForOwner =
                ItemMapper.toItemResponseDto(savedItem, null, null, commentsOut);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(bookingRepository.findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(anyLong(),
                any(), any(), any())).thenReturn(Optional.empty());
        when(bookingRepository.findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(anyLong(),
                any(), any(), any())).thenReturn(Optional.empty());
        when(commentRepository.findAllByItemId(itemId)).thenReturn(comments);

        ItemResponseDto result = itemService.getItemById(ownerId, itemId);

        InOrder inOrder = inOrder(itemRepository, commentRepository);
        inOrder.verify(itemRepository).findById(itemId);
        verify(bookingRepository)
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(),
                        any(), any(), any());
        verify(bookingRepository)
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(),
                        any(), any(), any());
        inOrder.verify(commentRepository).findAllByItemId(itemId);

        assertEquals(result, expectedItemForOwner);
        assertThat(result).hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "a spoon")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("lastBooking", null)
                .hasFieldOrPropertyWithValue("nextBooking", null)
                .hasFieldOrPropertyWithValue("comments", commentsOut);
    }

    @Test
    public void getById_whenItemNotExist_thenThrowItemNotExistException_NotInvokeAnyMore() {

        Long ownerId = 1L;

        Long itemId = 1L;

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        assertThrows(ItemNotExistException.class,
                () -> itemService.getItemById(ownerId, itemId),
                String.format("there is no item with id:%s", itemId));

        verify(itemRepository).findById(itemId);
    }

    @Test
    public void update_whenUserExists_ItemExists_AllItemDtoFieldsNotNullAndValid_ignoreId_invokeSave_returnUpdItem() {

        ItemDto itemDtoToUpdate = ItemDto.builder()
                .id(44L)
                .name("NewName")
                .description("NewDescription")
                .available(false)
                .build();

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent a spoon")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();

        Long itemId = 1L;
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder()
                .id(itemId)
                .build();

        Item updatedItem = ItemMapper.toItem(itemDtoToUpdate, owner, itemRequest)
                .toBuilder()
                .id(itemId)
                .build();
        ItemDto updatedItemDto = ItemMapper.toItemDto(updatedItem);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(savedItem));

        ItemDto result = itemService.updateItem(updatedItemDto, ownerId, itemId);

        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(itemRepository).findById(ownerId);
        inOrder.verify(itemRepository).save(updatedItem);

        assertEquals(result, updatedItemDto);
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "NewName")
                .hasFieldOrPropertyWithValue("description", "NewDescription")
                .hasFieldOrPropertyWithValue("available", false)
                .hasFieldOrPropertyWithValue("requestId", requestId);
    }

    @Test
    public void update_whenUserExists_ItemExists_OnlyValidNameToUpdate_ignoreId_invokeSave_returnUpdatedNameItem() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent a spoon")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();

        Long itemId = 1L;
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder()
                .id(itemId)
                .build();

        Item updatedItem = ItemMapper.toItem(itemDto, owner, itemRequest)
                .toBuilder()
                .id(itemId)
                .name("NewName")
                .build();
        ItemDto updatedItemDto = ItemMapper.toItemDto(updatedItem);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(savedItem));

        ItemDto result = itemService.updateItem(updatedItemDto, ownerId, itemId);

        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(itemRepository).findById(ownerId);
        inOrder.verify(itemRepository).save(updatedItem);

        assertEquals(result, updatedItemDto);
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "NewName")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("requestId", requestId);
    }

    @Test
    public void update_whenUserExists_ItemExists_OnlyValidDescriptionToUpdate_invokeSave_returnUpdDescriptionItem() {

        ItemDto itemDtoToUpdate = ItemDto.builder()
                .id(44L)
                .description("NewDescription")
                .build();

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent a spoon")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();

        Long itemId = 1L;
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder()
                .id(itemId)
                .build();

        Item updatedItem = ItemMapper.toItem(itemDto, owner, itemRequest)
                .toBuilder()
                .id(itemId)
                .description(itemDtoToUpdate.getDescription())
                .build();

        ItemDto updatedItemDto = ItemMapper.toItemDto(updatedItem);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(savedItem));
        when(itemRepository.save(updatedItem))
                .thenReturn(updatedItem);

        ItemDto result = itemService.updateItem(itemDtoToUpdate, ownerId, itemId);

        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(itemRepository).findById(ownerId);
        inOrder.verify(itemRepository).save(updatedItem);
        inOrder.verifyNoMoreInteractions();

        assertEquals(result, updatedItemDto);
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "a spoon")
                .hasFieldOrPropertyWithValue("description", "NewDescription")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("requestId", requestId);
    }

    @Test
    public void update_whenUserExists_ItemExists_OnlyValidAvailableToUpdate_ignoreId_invokeSave_returnUpdAvailItem() {

        ItemDto itemDtoToUpdate = ItemDto.builder()
                .id(99L)
                .available(false)
                .build();

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent a spoon")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();

        Long itemId = 1L;
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder()
                .id(itemId)
                .build();

        Item updatedItem = ItemMapper.toItem(itemDtoToUpdate, owner, null)
                .toBuilder()
                .id(itemId)
                .build();
        ItemDto updatedItemDto = ItemMapper.toItemDto(updatedItem);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(savedItem));

        ItemDto result = itemService.updateItem(updatedItemDto, ownerId, itemId);

        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(itemRepository).findById(ownerId);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "a spoon")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", false)
                .hasFieldOrPropertyWithValue("requestId", requestId);
    }

    @Test
    public void update_whenUserExists_ItemExists_InvalidFieldsToUpdate_invokeSave_returnNonUpdatedItem() {

        ItemDto invalidItemDtoToUpdate = ItemDto.builder()
                .name("")
                .description("")
                .available(null)
                .build();

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent a spoon")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();

        Long itemId = 1L;
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = item.toBuilder()
                .id(itemId)
                .build();
        ItemDto savedItemDto = ItemMapper.toItemDto(savedItem);

        Item updatedItem = ItemMapper.toItem(invalidItemDtoToUpdate, owner, itemRequest)
                .toBuilder()
                .id(itemId)
                .build();
        ItemDto updatedItemDto = ItemMapper.toItemDto(updatedItem);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(savedItem));

        ItemDto result = itemService.updateItem(updatedItemDto, ownerId, itemId);

        InOrder inOrder = inOrder(userRepository, itemRepository);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(itemRepository).findById(ownerId);
        inOrder.verify(itemRepository).save(savedItem);

        assertEquals(result, savedItemDto);
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemId)
                .hasFieldOrPropertyWithValue("name", "a spoon")
                .hasFieldOrPropertyWithValue("description", "new")
                .hasFieldOrPropertyWithValue("available", true)
                .hasFieldOrPropertyWithValue("requestId", requestId);
    }

    @Test
    public void update_whenUserNotExist_thenThrowsUserNotExistException_NotInvokeAnyMore() {

        Long ownerId = 1L;

        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();

        Long itemId = 1L;

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class,
                () -> itemService.updateItem(itemDto, ownerId, itemId),
                String.format("there is no user with id:%s", ownerId));

        verify(userRepository).findById(ownerId);
        verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    public void update_whenUserExist_butItemNot_thenThrowsItemNotExistException_NotInvokeAnyMore() {

        Long ownerId = 1L;

        Long itemId = 1L;

        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        assertThrows(ItemNotExistException.class,
                () -> itemService.updateItem(itemDto, ownerId, itemId),
                String.format("there is no item with id: %s", ownerId));

        verify(userRepository).findById(ownerId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void update_whenUserExist_itemExists_userIsNotOwner_thenThrowsAttempToUpdateNotYourItem_NotInvokeAnyMore() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long notOwnerId = 2L;

        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Notowner NotOwnerovich")
                .email("notowner777@yandex.ru")
                .build();

        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();
        Item item = ItemMapper.toItem(itemDto, owner, null);
        Item savedItem = item.toBuilder()
                .id(itemId)
                .build();

        when(userRepository.findById(notOwnerId))
                .thenReturn(Optional.of(notOwner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(savedItem));

        assertThrows(AttempToUpdateNotYourItemException.class,
                () -> itemService.updateItem(itemDto, notOwnerId, itemId),
                "you are not allow to edit the item, because you are not the owner");

        verify(userRepository).findById(notOwnerId);
        verify(itemRepository).findById(itemId);
        verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    public void getListByUser_returnListOfItems() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent a spoon")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        ItemDto itemDto1 = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();
        ItemDto itemDto2 = ItemDto.builder()
                .name("pram")
                .description("old")
                .available(true)
                .build();

        Long item1Id = 1L;
        Item item1 = ItemMapper.toItem(itemDto1, owner, itemRequest)
                .toBuilder().id(item1Id).build();

        Long item2Id = 2L;
        Item item2 = ItemMapper.toItem(itemDto2, owner, itemRequest)
                .toBuilder().id(item2Id).build();

        List<Item> items = List.of(item1, item2);

        Long lastBookingId = 1L;
        Booking lastBooking = Booking.builder()
                .id(lastBookingId)
                .booker(notOwner)
                .build();
        Long nextBookingId = 2L;
        Booking nextBooking = Booking.builder()
                .id(nextBookingId)
                .booker(notOwner)
                .build();
        BookingItemResponseDto lastBookingDto = BookingMapper.toBookingItemResponseDto(lastBooking);
        BookingItemResponseDto nextBookingDto = BookingMapper.toBookingItemResponseDto(nextBooking);

        Long comment1Id = 1L;
        Comment comment1 = Comment.builder()
                .id(comment1Id)
                .item(item1)
                .author(notOwner)
                .text("bad a spoon!")
                .created(LocalDateTime.now().minusWeeks(1))
                .build();
        Long comment2Id = 2L;
        Comment comment2 = Comment.builder()
                .id(comment2Id)
                .item(item1)
                .author(notOwner)
                .text("I change my mind: good a spoon!")
                .created(LocalDateTime.now())
                .build();
        List<Comment> comments = List.of(comment1, comment2);
        List<CommentResponseDto> commentsOut = CommentMapper.toCommentResponseDtoList(comments);

        ItemResponseDto itemOutDto1 = ItemMapper.toItemResponseDto(item1,
                lastBookingDto, nextBookingDto, commentsOut);
        ItemResponseDto itemOutDto2 = ItemMapper.toItemResponseDto(item2,
                lastBookingDto, nextBookingDto, Collections.emptyList());

        List<ItemResponseDto> expectedItemsListByUser = List.of(itemOutDto1, itemOutDto2);

        int from = 0;
        int size = 10;
        Pageable pageRequest = PageRequest.of(from / size, size);

        when(itemRepository.findAllByOwnerId(ownerId, pageRequest))
                .thenReturn(items);
        when(commentRepository.findAllByItemIn(items))
                .thenReturn(comments);
        when(bookingRepository
                .findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(any(), any(), any(), any()))
                .thenReturn(Optional.of(lastBooking));
        when(bookingRepository
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any()))
                .thenReturn(Optional.of(nextBooking));

        List<ItemResponseDto> result = itemService.getItemsOfOwner(ownerId, from, size);

        verify(itemRepository).findAllByOwnerId(ownerId, pageRequest);
        verify(commentRepository).findAllByItemIn(items);
        verify(bookingRepository, atLeast(1))
                .findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(any(), any(), any(), any());
        verify(bookingRepository, atLeast(1))
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any());

        assertEquals(result, expectedItemsListByUser);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getId(), item1Id);
        assertEquals(result.get(0).getName(), "a spoon");
        assertEquals(result.get(0).getDescription(), "new");
        assertEquals(result.get(0).getAvailable(), true);
        assertEquals(result.get(0).getLastBooking(), lastBookingDto);
        assertEquals(result.get(0).getLastBooking().getId(), lastBookingId);
        assertEquals(result.get(0).getNextBooking(), nextBookingDto);
        assertEquals(result.get(0).getNextBooking().getId(), nextBookingId);
        assertEquals(result.get(0).getComments(), commentsOut);
        assertEquals(result.get(0).getComments().get(0).getId(), comment1Id);
        assertEquals(result.get(0).getComments().get(0).getText(), "bad a spoon!");
        assertEquals(result.get(0).getComments().get(1).getId(), comment2Id);
        assertEquals(result.get(0).getComments().get(1).getText(), "I change my mind: good a spoon!");
        assertEquals(result.get(1).getId(), item2Id);
        assertEquals(result.get(1).getName(), "pram");
        assertEquals(result.get(1).getDescription(), "old");
        assertEquals(result.get(1).getAvailable(), true);
        assertEquals(result.get(1).getLastBooking(), lastBookingDto);
        assertEquals(result.get(1).getNextBooking(), nextBookingDto);
        assertEquals(result.get(1).getComments().size(), 0);
    }

    @Test
    public void getListByUserWithoutItems_returnEmptyListOfItems() {
        int from = 0;
        int size = 10;
        Pageable pageRequest = PageRequest.of(from / size, size);

        Long notOwnerId = 2L;

        List<ItemResponseDto> listByUser = Collections.emptyList();

        when(itemRepository.findAllByOwnerId(notOwnerId, pageRequest))
                .thenReturn(Collections.emptyList());
        when(commentRepository.findAllByItemIn(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        List<ItemResponseDto> result = itemService.getItemsOfOwner(notOwnerId, from, size);

        verify(itemRepository).findAllByOwnerId(notOwnerId, pageRequest);
        verify(commentRepository).findAllByItemIn(Collections.emptyList());
        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any());
        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any());

        assertEquals(result, listByUser);
        assertEquals(result.size(), 0);
    }

    @Test
    public void searchItemsBySubstring_returnListOfItems() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent a spoon")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        Long item1Id = 1L;
        ItemDto itemDto1 = ItemDto.builder()
                .id(item1Id)
                .name("a spoon")
                .description("new")
                .available(true)
                .build();
        Long item2Id = 2L;
        ItemDto itemDto2 = ItemDto.builder()
                .id(item2Id)
                .name("pram")
                .description("old")
                .available(true)
                .build();

        Item item1 = ItemMapper.toItem(itemDto1, owner, itemRequest);
        Item item2 = ItemMapper.toItem(itemDto2, owner, itemRequest);
        List<Item> items = List.of(item1, item2);
        Long lastBookingId = 1L;
        Booking lastBooking = Booking.builder()
                .id(lastBookingId)
                .booker(notOwner)
                .build();
        Long nextBookingId = 2L;
        Booking nextBooking = Booking.builder()
                .id(nextBookingId)
                .booker(notOwner)
                .build();
        BookingItemResponseDto lastBookingDto = BookingMapper.toBookingItemResponseDto(lastBooking);
        BookingItemResponseDto nextBookingDto = BookingMapper.toBookingItemResponseDto(nextBooking);

        Long comment1Id = 1L;
        Comment comment1 = Comment.builder()
                .id(comment1Id)
                .item(item1)
                .author(notOwner)
                .text("bad a spoon!")
                .created(LocalDateTime.now().minusWeeks(1))
                .build();
        Long comment2Id = 2L;
        Comment comment2 = Comment.builder()
                .id(comment2Id)
                .item(item1)
                .author(notOwner)
                .text("I change my mind: good a spoon!")
                .created(LocalDateTime.now())
                .build();
        List<Comment> comments = List.of(comment1, comment2);
        List<CommentResponseDto> commentsOut = CommentMapper.toCommentResponseDtoList(comments);

        ItemResponseDto itemOutDto1 = ItemMapper.toItemResponseDto(item1,
                lastBookingDto, nextBookingDto, commentsOut);
        ItemResponseDto itemOutDto2 = ItemMapper.toItemResponseDto(item2,
                lastBookingDto, nextBookingDto, Collections.emptyList());

        List<ItemResponseDto> expectedItemsListBySearch = List.of(itemOutDto1, itemOutDto2);

        int from = 0;
        int size = 10;
        Pageable pageRequest = PageRequest.of(from / size, size);

        when(itemRepository.searchItemBySubstring("text", pageRequest))
                .thenReturn(items);
        when(commentRepository.findAllByItemIn(items))
                .thenReturn(comments);
        when(bookingRepository
                .findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(any(), any(), any(), any()))
                .thenReturn(Optional.of(lastBooking));
        when(bookingRepository
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any()))
                .thenReturn(Optional.of(nextBooking));

        List<ItemResponseDto> result = itemService.findItemsByText("text", from, size);

        verify(itemRepository).searchItemBySubstring("text", pageRequest);
        verify(commentRepository).findAllByItemIn(items);
        verify(bookingRepository, atLeast(1))
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any());
        verify(bookingRepository, atLeast(1))
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any());

        assertEquals(result, expectedItemsListBySearch);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getId(), item1Id);
        assertEquals(result.get(0).getName(), "a spoon");
        assertEquals(result.get(0).getDescription(), "new");
        assertEquals(result.get(0).getAvailable(), true);
        assertEquals(result.get(0).getLastBooking(), lastBookingDto);
        assertEquals(result.get(0).getLastBooking().getId(), lastBookingId);
        assertEquals(result.get(0).getNextBooking(), nextBookingDto);
        assertEquals(result.get(0).getNextBooking().getId(), nextBookingId);
        assertEquals(result.get(0).getComments(), commentsOut);
        assertEquals(result.get(0).getComments().get(0).getId(), comment1Id);
        assertEquals(result.get(0).getComments().get(0).getText(), "bad a spoon!");
        assertEquals(result.get(0).getComments().get(1).getId(), comment2Id);
        assertEquals(result.get(0).getComments().get(1).getText(), "I change my mind: good a spoon!");
        assertEquals(result.get(1).getId(), item2Id);
        assertEquals(result.get(1).getName(), "pram");
        assertEquals(result.get(1).getDescription(), "old");
        assertEquals(result.get(1).getAvailable(), true);
        assertEquals(result.get(1).getLastBooking(), lastBookingDto);
        assertEquals(result.get(1).getNextBooking(), nextBookingDto);
        assertEquals(result.get(1).getComments().size(), 0);
    }

    @Test
    public void searchItemsBySubstringWithoutEmptyResult_returnEmptyListOfItems() {

        int from = 0;
        int size = 10;
        Pageable pageRequest = PageRequest.of(from / size, size);

        List<ItemResponseDto> expectedListBySearch = Collections.emptyList();

        when(itemRepository.searchItemBySubstring("text", pageRequest))
                .thenReturn(Collections.emptyList());
        when(commentRepository.findAllByItemIn(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        List<ItemResponseDto> result = itemService.findItemsByText("text", from, size);

        verify(itemRepository).searchItemBySubstring("text", pageRequest);
        verify(commentRepository).findAllByItemIn(Collections.emptyList());
        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any());
        verify(bookingRepository, never())
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(any(), any(), any(), any());

        assertEquals(result, expectedListBySearch);
        assertEquals(result.size(), 0);
    }

    @Test
    public void addComment_whenItemExists_AndUserExists_AndUserIsNotOwner_AndUserHasPastOrCurrentApprovedBookings() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Oleg")
                .email("Oleg@yandex.ru")
                .build();

        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("I would like to rent a spoon")
                .requester(notOwner)
                .created(LocalDateTime.now())
                .build();

        ItemDto itemDto1 = ItemDto.builder()
                .name("a spoon")
                .description("new")
                .available(true)
                .build();

        Long itemId = 1L;
        Item item = ItemMapper.toItem(itemDto1, owner, itemRequest);
        Item savedItem = item.toBuilder()
                .id(itemId)
                .build();

        Long lastBookingId = 1L;
        Booking lastBooking = Booking.builder()
                .id(lastBookingId)
                .booker(notOwner)
                .build();
        Long nextBookingId = 2L;
        Booking nextBooking = Booking.builder()
                .id(nextBookingId)
                .booker(notOwner)
                .build();

        Long commentId = 1L;
        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .itemId(itemId)
                .authorName(notOwner.getName())
                .text("bad a spoon!")
                .build();
        Comment comment = CommentMapper.toComment(commentRequestDto, notOwner, savedItem);
        Comment savedComment = comment.toBuilder()
                .id(commentId)
                .build();
        CommentResponseDto savedCommentRequestDto = CommentMapper.toCommentResponseDto(savedComment);

        when(userRepository.findById(notOwnerId))
                .thenReturn(Optional.of(notOwner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(savedItem));
        when(bookingRepository.findAllByItemIdAndBookerIdAndStatusAndStartIsBefore(anyLong(), any(), any(), any()))
                .thenReturn(List.of(lastBooking, nextBooking));
        when(commentRepository.save(any(Comment.class)))
                .thenReturn(savedComment);

        CommentResponseDto result = itemService.addComment(notOwnerId, itemId, commentRequestDto);

        InOrder inOrder = inOrder(userRepository, itemRepository, bookingRepository, commentRepository);

        inOrder.verify(userRepository).findById(notOwnerId);
        inOrder.verify(itemRepository).findById(itemId);
        inOrder.verify(bookingRepository).findAllByItemIdAndBookerIdAndStatusAndStartIsBefore(anyLong(),
                any(), any(), any());
        inOrder.verify(commentRepository).save(any(Comment.class));

        assertEquals(result, savedCommentRequestDto);
        assertThat(result)
                .hasFieldOrPropertyWithValue("id", commentId)
                .hasFieldOrPropertyWithValue("text", "bad a spoon!")
                .hasFieldOrPropertyWithValue("authorName", notOwner.getName())
                .hasFieldOrPropertyWithValue("itemId", itemId)
                .hasFieldOrProperty("created");

    }

    @Test
    public void addComment_whenItemNotExists_thenThrowsItemNotExistException_AndNotInvokeAnyMore() {

        Long itemId = 1L;

        Long notOwnerId = 1L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .itemId(itemId)
                .authorName(notOwner.getName())
                .text("broken spoon!")
                .build();

        when(userRepository.findById(notOwnerId))
                .thenReturn(Optional.of(notOwner));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        assertThrows(ItemNotExistException.class,
                () -> itemService.addComment(notOwnerId, itemId, commentRequestDto),
                String.format("there is no item with id:%s", itemId));

        verify(userRepository).findById(notOwnerId);
        verify(itemRepository).findById(itemId);
        verifyNoMoreInteractions(userRepository, bookingRepository, commentRepository);


    }

    @Test
    public void addComment_whenItemExists_ButUserNot_thenThrowsUserNotExistException_AndNotInvokeAnyMore() {

        Long itemId = 1L;
        Item savedItem = Item.builder()
                .id(itemId)
                .build();

        Long notOwnerId = 1L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .itemId(itemId)
                .authorName(notOwner.getName())
                .text("broken spoon!")
                .build();

        when(userRepository.findById(notOwnerId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class,
                () -> itemService.addComment(notOwnerId, itemId, commentRequestDto),
                String.format("there is no user with id:%s", notOwnerId));

        verify(userRepository).findById(notOwnerId);
        verifyNoMoreInteractions(itemRepository, bookingRepository, commentRepository);

    }

    @Test
    public void addComment_whenItemExists_andUserIsOwner_thenThrowsValidationException_AndNotInvokeAnyMore() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        Long itemId = 1L;
        Item savedItem = Item.builder()
                .id(itemId)
                .owner(owner)
                .build();

        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .itemId(itemId)
                .authorName(owner.getName())
                .text("broken spoon!")
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        assertThrows(ValidationException.class,
                () -> itemService.addComment(ownerId, itemId, commentRequestDto),
                "it's forbidden to comment your own item");

        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(ownerId);
        verifyNoMoreInteractions(bookingRepository, commentRepository);

    }

    @Test
    public void addComment_whenItemExists_UserIsNotOwner_butHasNotBookings_thenThrowsValidationExc_NotInvokeAnyMore() {

        Long ownerId = 1L;
        User owner = User.builder()
                .id(ownerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        Long itemId = 1L;
        Item savedItem = Item.builder()
                .id(itemId)
                .owner(owner)
                .build();

        Long notOwnerId = 2L;
        User notOwner = User.builder()
                .id(notOwnerId)
                .name("Alex")
                .email("Alex@yandex.ru")
                .build();

        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .itemId(itemId)
                .authorName(notOwner.getName())
                .text("broken spoon!")
                .build();

        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(savedItem));
        when(userRepository.findById(notOwnerId))
                .thenReturn(Optional.of(notOwner));
        when(bookingRepository.findAllByItemIdAndBookerIdAndStatusAndStartIsBefore(anyLong(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class,
                () -> itemService.addComment(notOwnerId, itemId, commentRequestDto),
                "it's forbidden to comment item you've never booked");

        verify(itemRepository).findById(itemId);
        verify(userRepository).findById(notOwnerId);
        verify(bookingRepository).findAllByItemIdAndBookerIdAndStatusAndStartIsBefore(anyLong(),
                any(), any(), any());
        verifyNoMoreInteractions(commentRepository);
    }
}
