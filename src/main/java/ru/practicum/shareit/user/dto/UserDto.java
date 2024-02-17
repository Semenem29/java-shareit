package ru.practicum.shareit.user.dto;

import lombok.*;
import ru.practicum.shareit.group.Create;
import ru.practicum.shareit.group.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class UserDto {

    Long id;

    @NotNull(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    String name;

    @NotNull(groups = {Create.class})
    @NotBlank(groups = {Create.class})
    @Email(groups = {Create.class, Update.class})
    String email;
}