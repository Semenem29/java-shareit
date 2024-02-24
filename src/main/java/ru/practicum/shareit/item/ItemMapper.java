package ru.practicum.shareit.item;

import org.springframework.lang.Nullable;
import ru.practicum.shareit.booking.dto.BookingItemResponseDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() == null ? null : item.getRequest().getId()
        );
    }

    public static Item toItem(ItemDto itemDto, User user, @Nullable ItemRequest itemRequest) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(user)
                .request(itemRequest)
                .build();
    }

    public static ItemResponseDto toItemResponseDto(ItemDto itemDto) {
        return new ItemResponseDto(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                null,
                null,
                null,
                Collections.emptyList()
        );
    }

    public static ItemResponseDto toItemResponseDto(Item item,
                                                    BookingItemResponseDto lastBooking,
                                                    BookingItemResponseDto nextBooking,
                                                    List<CommentResponseDto> comments) {
        return new ItemResponseDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBooking,
                nextBooking,
                item.getRequest() == null ? null : item.getRequest().getId(),
                comments
        );
    }

    public static ItemItemRequestDto toItemItemRequestDto(Item item) {
        return ItemItemRequestDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .requestId(item.getRequest() == null ? null : item.getRequest().getId())
                .available(item.getAvailable())
                .build();
    }

    public static List<ItemItemRequestDto> toItemItemRequestDtoList(List<Item> items) {
        return items.stream()
                .map(ItemMapper::toItemItemRequestDto)
                .collect(Collectors.toList());
    }
}
