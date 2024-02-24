package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class ItemRequestDto {
    Long id;
    @NotNull
    @NotBlank
    String description;
}
