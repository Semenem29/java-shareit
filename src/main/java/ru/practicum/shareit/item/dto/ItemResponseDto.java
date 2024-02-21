package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.BookingItemResponseDto;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class ItemResponseDto {

    Long id;
    @NonNull
    @NotBlank
    String name;
    @NonNull
    @NotBlank
    String description;
    @NonNull
    Boolean available;
    BookingItemResponseDto lastBooking;
    BookingItemResponseDto nextBooking;
    Long requestId;
    List<CommentResponseDto> comments;
}
