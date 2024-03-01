package ru.practicum.shareit.request.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ItemRequestNotExistException;
import ru.practicum.shareit.exception.UserNotExistException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemJPARepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.store.ItemRequestJPARepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJPARepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ItemRequestJPAServiceImpl implements ItemRequestJPAService {
    ItemRequestJPARepository itemRequestJpaRepository;
    UserJPARepository userJPARepository;
    ItemJPARepository itemJPARepository;

    @Override
    @Transactional
    public ItemRequestResponseDto createRequest(Long requesterId, ItemRequestDto itemRequestDto) {
        User user = getUserOrThrow(requesterId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        ItemRequest savedItemRequest = itemRequestJpaRepository.save(itemRequest);
        ItemRequestResponseDto itemRequestResponseDto = ItemRequestMapper.toItemRequestResponseDto(savedItemRequest, null);
        log.info("creation request: user with id={} have created request:{}", requesterId, itemRequestResponseDto);
        return itemRequestResponseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestResponseDto> findAllByRequester(Long requesterId) {
        getUserOrThrow(requesterId);
        List<ItemRequest> requests = itemRequestJpaRepository.findAllByRequesterIdOrderByCreatedDesc(requesterId);
        Map<ItemRequest, List<Item>> requestTable = getItemsOfItemRequest(requests);
        List<ItemRequestResponseDto> resultItemRequests = requests.stream()
                .map(item -> ItemRequestMapper
                        .toItemRequestResponseDto(item, requestTable.getOrDefault(item, Collections.emptyList())))
                .collect(Collectors.toList());
        logItemRequestList(resultItemRequests);
        return resultItemRequests;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestResponseDto getRequestById(Long requesterId, Long requestId) {
        getUserOrThrow(requesterId);
        ItemRequest itemRequest = getItemRequestOrThrow(requestId);
        List<Item> items = itemJPARepository.findAllByRequestId(requestId);
        ItemRequestResponseDto itemRequestResponseDto = ItemRequestMapper.toItemRequestResponseDto(itemRequest, items);
        log.info("provided info about itemRequest: {}", itemRequestResponseDto);
        return itemRequestResponseDto;
    }

    @Override
    @Transactional
    public List<ItemRequestResponseDto> getAllOtherRequests(Long requesterId, Integer from, Integer size) {
        getUserOrThrow(requesterId);
        int page = from / size;
        Pageable pageRequest = PageRequest.of(page, size);

        List<ItemRequest> itemRequests = itemRequestJpaRepository
                .findAllByRequesterIdIsNotOrderByCreatedDesc(requesterId, pageRequest);

        Map<ItemRequest, List<Item>> requests = getItemsOfItemRequest(itemRequests);

        List<ItemRequestResponseDto> requestResponseDtos = itemRequests.stream()
                .map(item -> ItemRequestMapper
                        .toItemRequestResponseDto(item, requests.getOrDefault(item, Collections.emptyList())))
                .collect(Collectors.toList());
        logItemRequestList(requestResponseDtos);
        return requestResponseDtos;
    }

    private void logItemRequestList(List<ItemRequestResponseDto> resultItemRequests) {
        String requests = resultItemRequests.stream()
                .map(ItemRequestResponseDto::toString)
                .collect(Collectors.joining(", "));
        log.info("provided info about item request llist: {}", requests);
    }

    private Map<ItemRequest, List<Item>> getItemsOfItemRequest(List<ItemRequest> requests) {
        List<Item> items = itemJPARepository.findAllByRequestIn(requests);
        log.info("uesers items: {}", items);
        if (items.isEmpty()) {
            return requests.stream()
                    .collect(Collectors.toMap(
                            request -> request,
                            request -> new ArrayList<>()
                    ));
        }

        return items.stream()
                .collect(Collectors.groupingBy(Item::getRequest));
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

    private ItemRequest getItemRequestOrThrow(Long itemRequestId) {
        Optional<ItemRequest> itemRequestOpt = itemRequestJpaRepository.findById(itemRequestId);
        if (itemRequestOpt.isEmpty()) {
            String message = "there is no item request with id: " + itemRequestId;
            log.error("ItemRequestNotExistException: " + message);
            throw new ItemRequestNotExistException(message);
        }

        return itemRequestOpt.get();
    }
}
