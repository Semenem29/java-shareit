package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import ru.practicum.shareit.item.dto.ItemItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class ItemRequestResponseDto {
    Long id;
    String description;
    LocalDateTime created;
    List<ItemItemRequestDto> items;
}
