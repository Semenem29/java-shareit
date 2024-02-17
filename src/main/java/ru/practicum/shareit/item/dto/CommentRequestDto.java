package ru.practicum.shareit.item.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class CommentRequestDto {
    Long id;
    @NotNull
    @NotBlank
    String text;
    String authorName;
    Long itemId;
}
