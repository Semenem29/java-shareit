package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.ItemNotExistException;
import ru.practicum.shareit.exception.ItemRequestNotExistException;
import ru.practicum.shareit.exception.UserNotExistException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemJPARepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {
    @InjectMocks
    ItemRequestJPAServiceImpl itemRequestService;
    @Mock
    UserJPARepository userRepository;
    @Mock
    ItemJPARepository itemRepository;
    @Mock
    ItemRequestJPARepository itemRequestRepository;
    @Captor
    ArgumentCaptor<ItemRequest> requestCaptor;

    @Test
    public void create_whenUserFound_thenInvokeSave_constructAndReturnResult() {

        Long requesterId = 1L;
        User requester = User.builder()
                .id(requesterId)
                .name("john")
                .email("cena@yandex.ru")
                .build();

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();

        ItemRequest itemRequest = ItemRequestMapper
                .toItemRequest(itemRequestDto, requester);

        Long itemRequestId = 1L;
        ItemRequest savedItemRequest = itemRequest.toBuilder()
                .id(itemRequestId)
                .build();

        ItemRequestResponseDto expectedItemRequest = ItemRequestMapper
                .toItemRequestResponseDto(savedItemRequest, null);

        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemRequestRepository.save(any()))
                .thenReturn(savedItemRequest);

        ItemRequestResponseDto result = itemRequestService.createRequest(requesterId, itemRequestDto);

        InOrder inOrder = inOrder(userRepository, itemRequestRepository);
        inOrder.verify(userRepository).findById(requesterId);
        inOrder.verify(itemRequestRepository).save(requestCaptor.capture());

        assertEquals(result, expectedItemRequest);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", itemRequestId)
                .hasFieldOrPropertyWithValue("description", "I would like to book a spoon")
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("items", Collections.emptyList());

        ItemRequest capturedItemRequest = requestCaptor.getValue();
        assertEquals(capturedItemRequest.getDescription(), itemRequest.getDescription());
        assertEquals(capturedItemRequest.getRequester(), requester);
        assertEquals(capturedItemRequest.getCreated().getClass(), LocalDateTime.class);
    }

    @Test
    public void create_whenUserNotFound_thenThrowsObjectNotFound_AndNotInvokeSave() {

        Long requesterId = 1L;

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();

        when(userRepository.findById(requesterId))
                .thenReturn(Optional.empty());


        assertThrows(UserNotExistException.class,
                () -> itemRequestService.createRequest(requesterId, itemRequestDto),
                String.format("there is no user with id: %s", requesterId));

        verify(userRepository).findById(requesterId);
        verifyNoInteractions(itemRequestRepository);
    }

    @Test
    public void getRequestById_whenUserFound_ItemRequestFound_thenReturnItemRequestWithItemsIfTheyPresent() {

        Long requesterId = 1L;
        User requester = User.builder()
                .id(requesterId)
                .name("hector")
                .email("hector@yandex.ru")
                .build();

        User owner = User.builder()
                .id(requesterId)
                .name("fish")
                .email("fishfish@yandex.ru")
                .build();

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        ItemRequest itemRequest = ItemRequestMapper
                .toItemRequest(itemRequestDto, requester);

        Long requestId = 1L;
        ItemRequest savedItemRequest = itemRequest.toBuilder()
                .id(requestId)
                .build();

        Long item1Id = 1L;
        Item item1 = Item.builder()
                .id(item1Id)
                .owner(owner)
                .name("spoon")
                .description("silver")
                .available(true)
                .request(savedItemRequest)
                .build();
        ItemDto item1dto = ItemMapper.toItemDto(item1);
        Long item2Id = 2L;
        Item item2 = Item.builder()
                .id(item2Id)
                .owner(owner)
                .name("spoon")
                .description("black")
                .available(true)
                .request(savedItemRequest)
                .build();
        ItemDto item2dto = ItemMapper.toItemDto(item2);

        List<Item> items = List.of(item1, item2);

        List<ItemDto> itemsList = List.of(item1dto, item2dto);

        ItemRequestResponseDto expectedItemRequest = ItemRequestMapper
                .toItemRequestResponseDto(savedItemRequest, items);

        when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.of(savedItemRequest));
        when(itemRepository.findAllByRequestId(requestId))
                .thenReturn(items);

        ItemRequestResponseDto result = itemRequestService.getRequestById(requesterId, requestId);

        InOrder inOrder = inOrder(userRepository, itemRequestRepository, itemRepository);
        inOrder.verify(itemRequestRepository).findById(requestId);
        inOrder.verify(itemRepository).findAllByRequestId(requestId);

        assertEquals(result, expectedItemRequest);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("description", "I would like to book a spoon")
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("items", itemsList);

        assertEquals(result.getItems().get(0), item1dto);
        assertEquals(result.getItems().get(1), item2dto);
    }

    @Test
    public void getRequestById_whenUserNotExist_thenThrowsUserNotExistException_NotInvokeAnyMore() {

        Long userId = 1L;

        Long requestId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class,
                () -> itemRequestService.getRequestById(userId, requestId),
                String.format("there is no user with id: %s", userId));

        verify(userRepository).findById(userId);
        verifyNoInteractions(itemRequestRepository);
    }

    @Test
    public void getRequestById_whenUserFound_whenItemRequestNotFound_thenThrowsUserNotExistException() {

        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .build();

        Long requestId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.empty());

        assertThrows(ItemRequestNotExistException.class,
                () -> itemRequestService.getRequestById(userId, requestId),
                String.format("there is no item request with id: %s", requestId));

        verify(userRepository).findById(userId);
        verify(itemRequestRepository).findById(requestId);
    }

    @Test
    public void getOwnRequests_whenUserExists_invokeRequestRepository_invokeItemRepository_constructAndReturnList() {

        int from = 0;
        int size = 10;
        int page = from / size;
        Pageable pageRequest = PageRequest.of(page, size);

        Long requesterId = 1L;
        User requester = User.builder()
                .id(requesterId)
                .name("Boris")
                .email("Boris@yandex.ru")
                .build();

        Long ownerId = 2L;
        User owner = User.builder()
                .id(ownerId)
                .name("Kate")
                .email("Kate@yandex.ru")
                .build();

        ItemRequestDto itemRequestDto1 = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        ItemRequestDto itemRequestDto2 = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        ItemRequestDto itemRequestDto3 = ItemRequestDto.builder()
                .description("I need bed")
                .build();

        ItemRequest itemRequest1 = ItemRequestMapper
                .toItemRequest(itemRequestDto1, requester);
        ItemRequest itemRequest2 = ItemRequestMapper
                .toItemRequest(itemRequestDto2, requester);
        ItemRequest itemRequest3 = ItemRequestMapper
                .toItemRequest(itemRequestDto3, requester);

        Long itemRequest1Id = 1L;
        ItemRequest savedItemRequest1 = itemRequest1.toBuilder()
                .id(itemRequest1Id)
                .build();
        Long itemRequest2Id = 2L;
        ItemRequest savedItemRequest2 = itemRequest2.toBuilder()
                .id(itemRequest2Id)
                .build();
        Long itemRequest3Id = 3L;
        ItemRequest savedItemRequest3 = itemRequest3.toBuilder()
                .id(itemRequest3Id)
                .build();

        List<ItemRequest> itemRequests = List.of(savedItemRequest1, savedItemRequest2, savedItemRequest3);

        Long item1Id = 1L;
        Item item1 = Item.builder()
                .id(item1Id)
                .owner(owner)
                .name("a spoon")
                .description("black")
                .available(true)
                .request(savedItemRequest1)
                .build();

        Long item2Id = 2L;
        Item item2 = Item.builder()
                .id(item2Id)
                .owner(owner)
                .name("a spoon")
                .description("silver")
                .available(true)
                .request(savedItemRequest2)
                .build();

        Long item3Id = 3L;
        Item item3 = Item.builder()
                .id(item3Id)
                .owner(owner)
                .name("bed")
                .available(true)
                .request(savedItemRequest3)
                .build();

        List<Item> request1Items = List.of(item1);
        List<Item> request2Items = List.of(item2);
        List<Item> request3Items = List.of(item3);

        List<Item> allItems = List.of(item1, item2, item3);

        ItemRequestResponseDto expectedItemRequest1 = ItemRequestMapper
                .toItemRequestResponseDto(savedItemRequest1, request1Items);
        ItemRequestResponseDto expectedItemRequest2 = ItemRequestMapper
                .toItemRequestResponseDto(savedItemRequest2, request2Items);
        ItemRequestResponseDto expectedItemRequest3 = ItemRequestMapper
                .toItemRequestResponseDto(savedItemRequest3, request3Items);

        List<ItemRequestResponseDto> expectedItemRequests = List.of(expectedItemRequest1,
                expectedItemRequest2, expectedItemRequest3);

        when(userRepository.findById(requesterId)).
                thenReturn(Optional.of(requester));
        when(itemRequestRepository.findAllByRequesterIdIsNotOrderByCreatedDesc(requesterId, pageRequest))
                .thenReturn(itemRequests);
        when(itemRepository.findAllByRequestIn(itemRequests))
                .thenReturn(allItems);

        List<ItemRequestResponseDto> result = itemRequestService.getAllOtherRequests(requesterId, from, size);

        InOrder inOrder = inOrder(userRepository, itemRequestRepository, itemRepository);
        inOrder.verify(userRepository).findById(requesterId);
        inOrder.verify(itemRequestRepository).findAllByRequesterIdIsNotOrderByCreatedDesc(requesterId, pageRequest);
        inOrder.verify(itemRepository).findAllByRequestIn(itemRequests);

        assertEquals(expectedItemRequests, result);

        assertThat(result).asList()
                .hasSize(3);

        assertEquals(result.get(0), expectedItemRequest1);
        assertEquals(result.get(0).getItems().size(), 1);
        assertEquals(result.get(0).getDescription(), "I would like to book a spoon");
        assertEquals(result.get(0).getItems().get(0).getName(), "a spoon");
        assertEquals(result.get(0).getItems().get(0).getDescription(), "black");
        assertEquals(result.get(1), expectedItemRequest2);
        assertEquals(result.get(1).getItems().size(), 1);
        assertEquals(result.get(1).getDescription(), "I would like to book a spoon");
        assertEquals(result.get(1).getItems().get(0).getName(), "a spoon");
        assertEquals(result.get(1).getItems().get(0).getDescription(), "silver");
        assertEquals(result.get(2), expectedItemRequest3);
        assertEquals(result.get(2).getItems().size(), 1);
        assertEquals(result.get(2).getDescription(), "I need bed");
        assertEquals(result.get(2).getItems().get(0).getName(), "bed");
    }

    @Test
    public void getOwnRequests_whenUserNotExist_thenThrowUserNotExistException_NotInvokeAnyMore() {

        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class,
                () -> itemRequestService.findAllByRequester(userId),
                String.format("there is no user with id: %s", userId));

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(itemRequestRepository, itemRepository);
    }

    @Test
    public void getgetAllOtherRequests_whenUserExists_invokeRequestRepository_invokeItemRep_constructAndReturnList() {

        Long ownerId = 2L;
        User owner = User.builder()
                .id(ownerId)
                .name("Peter")
                .email("Peter@yandex.ru")
                .build();

        ItemRequestDto itemRequestDto1 = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();
        ItemRequestDto itemRequestDto2 = ItemRequestDto.builder()
                .description("I need a bed")
                .build();

        ItemRequest itemRequest1 = ItemRequestMapper
                .toItemRequest(itemRequestDto1, owner);
        ItemRequest itemRequest2 = ItemRequestMapper
                .toItemRequest(itemRequestDto2, owner);

        Long itemRequest1Id = 1L;
        ItemRequest savedItemRequest1 = itemRequest1.toBuilder()
                .id(itemRequest1Id)
                .build();
        Long itemRequest2Id = 2L;
        ItemRequest savedItemRequest2 = itemRequest2.toBuilder()
                .id(itemRequest2Id)
                .build();

        List<ItemRequest> itemRequests = List.of(savedItemRequest1, savedItemRequest2);

        Long item1Id = 1L;
        Item item1 = Item.builder()
                .id(item1Id)
                .owner(owner)
                .name("a spoon")
                .description("new")
                .available(true)
                .request(savedItemRequest1)
                .build();

        Long item2Id = 2L;
        Item item2 = Item.builder()
                .id(item2Id)
                .owner(owner)
                .name("a spoon")
                .description("old")
                .available(true)
                .request(savedItemRequest1)
                .build();

        Long item3Id = 3L;
        Item item3 = Item.builder()
                .id(item3Id)
                .owner(owner)
                .name("a bed")
                .available(true)
                .request(savedItemRequest2)
                .build();

        List<Item> request1Items = List.of(item1, item2);
        List<Item> request2Items = List.of(item3);

        List<Item> allItems = List.of(item1, item2, item3);

        ItemRequestResponseDto expectedItemRequest1 = ItemRequestMapper
                .toItemRequestResponseDto(savedItemRequest1, request1Items);
        ItemRequestResponseDto expectedItemRequest2 = ItemRequestMapper
                .toItemRequestResponseDto(savedItemRequest2, request2Items);

        List<ItemRequestResponseDto> expectedItemRequests = List.of(expectedItemRequest1, expectedItemRequest2);

        int from = 10;
        int size = 10;

        PageRequest page = PageRequest.of(from / size, size);

        when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRequestRepository.findAllByRequesterIdIsNotOrderByCreatedDesc(ownerId, page))
                .thenReturn(itemRequests);
        when(itemRepository.findAllByRequestIn(itemRequests))
                .thenReturn(allItems);

        List<ItemRequestResponseDto> result = itemRequestService.getAllOtherRequests(ownerId, from, size);

        InOrder inOrder = inOrder(userRepository, itemRequestRepository, itemRepository);
        inOrder.verify(userRepository).findById(ownerId);
        inOrder.verify(itemRequestRepository).findAllByRequesterIdIsNotOrderByCreatedDesc(ownerId, page);
        inOrder.verify(itemRepository).findAllByRequestIn(itemRequests);

        assertEquals(expectedItemRequests, result);

        assertThat(result).asList()
                .hasSize(2);

        assertEquals(result.get(0), expectedItemRequest1);
        assertEquals(result.get(0).getItems().size(), 2);
        assertEquals(result.get(0).getDescription(), "I would like to book a spoon");
        assertEquals(result.get(0).getItems().get(0).getName(), "a spoon");
        assertEquals(result.get(0).getItems().get(1).getName(), "a spoon");
        assertEquals(result.get(0).getItems().get(0).getDescription(), "new");
        assertEquals(result.get(1), expectedItemRequest2);
        assertEquals(result.get(1).getItems().size(), 1);
        assertEquals(result.get(1).getDescription(), "I need a bed");
        assertEquals(result.get(1).getItems().get(0).getName(), "a bed");
    }

    @Test
    public void getOtherUsersRequests_whenUserNotExist_thenThrowObjectNotFound_NotInvokeAnyMore() {

        Long userId = 1L;
        Integer from = 0;
        Integer size = 10;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class,
                () -> itemRequestService.getAllOtherRequests(userId, from, size),
                String.format("there is no user with id: %s", userId));

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(itemRequestRepository, itemRepository);
    }

}
