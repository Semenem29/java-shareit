package ru.practicum.shareit.request;

import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemItemRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequestDto toRequestDto(ItemRequest request) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto requestDto, User user) {
        return ItemRequest.builder()
                .id(requestDto.getId())
                .description(requestDto.getDescription())
                .requester(user)
                .created(LocalDateTime.now())
                .build();
    }

    public static List<ItemRequestDto> toItemRequestDtoList(List<ItemRequest> requests) {
        return requests.stream()
                .map(ItemRequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    public static ItemRequestResponseDto toItemRequestResponseDto(ItemRequest request, List<Item> items) {
        return ItemRequestResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items == null ? new ArrayList<>() : ItemMapper.toItemItemRequestDtoList(items))
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestResponseDto itemRequestResponseDto, User user) {
        return ItemRequest.builder()
                .id(itemRequestResponseDto.getId())
                .description(itemRequestResponseDto.getDescription())
                .created(itemRequestResponseDto.getCreated())
                .requester(user)
                .build();
    }
}
