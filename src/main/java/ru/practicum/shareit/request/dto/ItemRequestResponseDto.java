package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class ItemRequestResponseDto {
    Long id;
    String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    LocalDateTime created;
    List<ItemDto> items;
}
