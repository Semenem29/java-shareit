package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.ItemRequestNotExistException;
import ru.practicum.shareit.exception.UserNotExistException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemJPAService;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserJPAService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemRequestServiceIntegrationTest {
    @Autowired
    private ItemRequestJPAService itemRequestService;
    @Autowired
    private UserJPAService userService;
    @Autowired
    private ItemJPAService itemService;

    @Test
    public void shouldCreateItemRequest_andGetByExistingId() {

        Long requesterId = 1L;
        User requester = User.builder()
                .name("Boris")
                .email("Eltsin@yandex.ru")
                .build();
        UserDto requesterDto = UserMapper.toUserDto(requester);

        userService.createUser(requesterDto);

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();

        Long requestId = 1L;

        ItemRequestResponseDto result = itemRequestService.createRequest(requesterId, itemRequestDto);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("description", "I would like to book a spoon")
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("items", Collections.emptyList());

        ItemRequestResponseDto resultByGet = itemRequestService.getRequestById(requesterId, requestId);

        assertThat(resultByGet)
                .hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("description", "I would like to book a spoon")
                .hasFieldOrProperty("created")
                .hasFieldOrPropertyWithValue("items", Collections.emptyList());
    }

    @Test
    public void shouldFailCreate_ThrowUserNotExistException() {

        Long requesterId = 1L;

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("I would like to book a spoon")
                .build();

        assertThrows(UserNotExistException.class,
                () -> itemRequestService.createRequest(requesterId, itemRequestDto),
                String.format("there is no user with id: %s", requesterId));
    }

    @Test
    public void shouldFailGetByRequestId_throwItemRequestNotExistException() {

        Long requesterId = 1L;
        User requester = User.builder()
                .name("Boris")
                .email("Eltsin@yandex.ru")
                .build();
        UserDto requesterDto = UserMapper.toUserDto(requester);

        userService.createUser(requesterDto);

        Long requestId = 1L;

        assertThrows(ItemRequestNotExistException.class,
                () -> itemRequestService.getRequestById(requesterId, requestId),
                String.format("there is no item request with id: %s", requestId));
    }

    @Test
    public void shouldGetOwnRequests() {

        Long requesterId = 1L;
        User requester = User.builder()
                .id(requesterId)
                .name("Boris")
                .email("eltsin@yandex.ru")
                .build();
        UserDto requesterDto = UserMapper.toUserDto(requester);
        userService.createUser(requesterDto);

        Long ownerId = 2L;
        User owner = User.builder()
                .id(ownerId)
                .name("Joseph")
                .email("stalin@yandex.ru")
                .build();
        UserDto ownerDto = UserMapper.toUserDto(owner);
        userService.createUser(ownerDto);

        Long itemRequest1Id = 1L;
        ItemRequestDto itemRequestDto1 = ItemRequestDto.builder()
                .description("I would like to book spoon")
                .build();
        ItemRequest savedRequest1 = ItemRequestMapper.toItemRequest(itemRequestDto1, requester)
                .toBuilder()
                .id(itemRequest1Id)
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto1);
        Long itemRequest2Id = 2L;
        ItemRequestDto itemRequestDto2 = ItemRequestDto.builder()
                .description("I need a bed")
                .build();
        ItemRequest savedRequest2 = ItemRequestMapper.toItemRequest(itemRequestDto2, requester)
                .toBuilder()
                .id(itemRequest2Id)
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto2);
        Long itemRequest3Id = 3L;
        ItemRequestDto itemRequestDto3 = ItemRequestDto.builder()
                .description("I need a jet")
                .build();
        ItemRequest savedRequest3 = ItemRequestMapper.toItemRequest(itemRequestDto3, requester)
                .toBuilder()
                .id(itemRequest3Id)
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto3);

        Item item1 = Item.builder()
                .owner(owner)
                .name("spoon")
                .description("silver")
                .available(true)
                .request(savedRequest1)
                .build();
        ItemDto itemDto1 = ItemMapper.toItemDto(item1);
        itemService.createItem(itemDto1, ownerId);

        Long item2Id = 2L;
        Item item2 = Item.builder()
                .id(item2Id)
                .owner(owner)
                .name("bed")
                .description("sofa")
                .available(true)
                .request(savedRequest2)
                .build();
        ItemDto itemDto2 = ItemMapper.toItemDto(item2);
        itemService.createItem(itemDto2, ownerId);

        Long item3Id = 3L;
        Item item3 = Item.builder()
                .id(item3Id)
                .owner(owner)
                .name("jet")
                .available(true)
                .description("dry jet")
                .request(savedRequest3)
                .build();
        ItemDto itemDto3 = ItemMapper.toItemDto(item3);
        itemService.createItem(itemDto3, ownerId);

        List<ItemRequestResponseDto> result = itemRequestService.findAllByRequester(requesterId);

        assertThat(result).asList().hasSize(3);

        assertEquals(result.get(0).getItems().size(), 1);
        assertEquals(result.get(0).getDescription(), "I need a jet");
        assertEquals(result.get(0).getItems().get(0).getName(), "jet");
        assertEquals(result.get(0).getItems().get(0).getDescription(), "dry jet");
        assertEquals(result.get(0).getItems().get(0).getAvailable(), true);
        assertEquals(result.get(1).getItems().size(), 1);
        assertEquals(result.get(1).getDescription(), "I need a bed");
        assertEquals(result.get(1).getItems().get(0).getName(), "bed");
        assertEquals(result.get(1).getItems().get(0).getDescription(), "sofa");
        assertEquals(result.get(1).getItems().get(0).getAvailable(), true);
        assertEquals(result.get(2).getItems().size(), 1);
        assertEquals(result.get(2).getDescription(), "I would like to book spoon");
        assertEquals(result.get(2).getItems().get(0).getName(), "spoon");
        assertEquals(result.get(2).getItems().get(0).getDescription(), "silver");
        assertEquals(result.get(2).getItems().get(0).getAvailable(), true);
    }

    @Test
    public void shouldFailGetOwnRequestsIfUserNotFound() {

        int from = 0;
        int size = 10;

        Long requesterId = 1L;

        assertThrows(UserNotExistException.class,
                () -> itemRequestService.getAllOtherRequests(requesterId, from, size),
                String.format("there is no user with id: %s", requesterId));

    }

    @Test
    public void shouldGetOtherUsersRequests() {

        int from = 0;
        int size = 10;

        Long requesterId = 1L;
        User requester = User.builder()
                .id(requesterId)
                .name("Boris")
                .email("eltsin@yandex.ru")
                .build();
        UserDto requesterDto = UserMapper.toUserDto(requester);
        userService.createUser(requesterDto);

        Long ownerId = 2L;
        User owner = User.builder()
                .id(ownerId)
                .name("Joseph")
                .email("stalin@yandex.ru")
                .build();
        UserDto ownerDto = UserMapper.toUserDto(owner);
        userService.createUser(ownerDto);

        Long itemRequest1Id = 1L;
        ItemRequestDto itemRequestDto1 = ItemRequestDto.builder()
                .description("I would like to book spoon")
                .build();
        ItemRequest savedRequest1 = ItemRequestMapper.toItemRequest(itemRequestDto1, requester)
                .toBuilder()
                .id(itemRequest1Id)
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto1);
        Long itemRequest2Id = 2L;
        ItemRequestDto itemRequestDto2 = ItemRequestDto.builder()
                .description("I need a bed")
                .build();
        ItemRequest savedRequest2 = ItemRequestMapper.toItemRequest(itemRequestDto2, requester)
                .toBuilder()
                .id(itemRequest2Id)
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto2);
        Long itemRequest3Id = 3L;
        ItemRequestDto itemRequestDto3 = ItemRequestDto.builder()
                .description("I need a jet")
                .build();
        ItemRequest savedRequest3 = ItemRequestMapper.toItemRequest(itemRequestDto3, requester)
                .toBuilder()
                .id(itemRequest3Id)
                .build();
        itemRequestService.createRequest(requesterId, itemRequestDto3);

        Item item1 = Item.builder()
                .owner(owner)
                .name("spoon")
                .description("silver")
                .available(true)
                .request(savedRequest1)
                .build();
        ItemDto itemDto1 = ItemMapper.toItemDto(item1);
        itemService.createItem(itemDto1, ownerId);

        Long item2Id = 2L;
        Item item2 = Item.builder()
                .id(item2Id)
                .owner(owner)
                .name("bed")
                .description("sofa")
                .available(true)
                .request(savedRequest2)
                .build();
        ItemDto itemDto2 = ItemMapper.toItemDto(item2);
        itemService.createItem(itemDto2, ownerId);

        Long item3Id = 3L;
        Item item3 = Item.builder()
                .id(item3Id)
                .owner(owner)
                .name("jet")
                .available(true)
                .description("dry jet")
                .request(savedRequest3)
                .build();
        ItemDto itemDto3 = ItemMapper.toItemDto(item3);
        itemService.createItem(itemDto3, ownerId);

        List<ItemRequestResponseDto> result = itemRequestService.getAllOtherRequests(ownerId, from, size);

        assertThat(result).asList().hasSize(3);

        assertEquals(result.get(0).getItems().size(), 1);
        assertEquals(result.get(0).getDescription(), "I need a jet");
        assertEquals(result.get(0).getItems().get(0).getName(), "jet");
        assertEquals(result.get(0).getItems().get(0).getDescription(), "dry jet");
        assertEquals(result.get(0).getItems().get(0).getAvailable(), true);
        assertEquals(result.get(1).getItems().size(), 1);
        assertEquals(result.get(1).getDescription(), "I need a bed");
        assertEquals(result.get(1).getItems().get(0).getName(), "bed");
        assertEquals(result.get(1).getItems().get(0).getDescription(), "sofa");
        assertEquals(result.get(1).getItems().get(0).getAvailable(), true);
        assertEquals(result.get(2).getItems().size(), 1);
        assertEquals(result.get(2).getDescription(), "I would like to book spoon");
        assertEquals(result.get(2).getItems().get(0).getName(), "spoon");
        assertEquals(result.get(2).getItems().get(0).getDescription(), "silver");
        assertEquals(result.get(2).getItems().get(0).getAvailable(), true);
    }

    @Test
    public void shouldFailGetOtherUserRequestsIfUserNotFound() {

        int from = 0;
        int size = 10;

        Long requesterId = 1L;

        assertThrows(UserNotExistException.class,
                () -> itemRequestService.getAllOtherRequests(requesterId, from, size),
                String.format("there is no user with id: %s", requesterId));

    }
}
