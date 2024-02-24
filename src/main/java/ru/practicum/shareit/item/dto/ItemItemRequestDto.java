package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import ru.practicum.shareit.booking.dto.BookingItemResponseDto;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class ItemItemRequestDto {

    Long id;
    @NonNull
    @NotBlank
    String name;
    @NonNull
    @NotBlank
    String description;
    Long requestId;
    Boolean available;
}
