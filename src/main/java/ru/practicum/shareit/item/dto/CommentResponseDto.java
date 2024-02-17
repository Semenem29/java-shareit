package ru.practicum.shareit.item.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class CommentResponseDto {
    Long id;
    @NotNull
    @NotBlank
    String text;
    String authorName;
    Long itemId;
    LocalDateTime created;
}
