package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.group.Create;
import ru.practicum.shareit.group.Update;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserJPAService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserJPAService userService;

    @PostMapping
    @Validated(Create.class)
    public UserDto createUser(@NotNull @Valid @RequestBody UserDto userDto) {
        log.info("POST-request: create a user={}", userDto);
        return userService.createUser(userDto);
    }

    @GetMapping
    public Collection<UserDto> getAllUsers() {
        log.info("GET-request: get all users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@NotNull @PathVariable Long id) {
        log.info("GET-request: get user with id={}", id);
        return userService.getUserById(id);
    }

    @PatchMapping("/{id}")
    @Validated(Update.class)
    public UserDto updateUser(@NotNull @Valid @RequestBody UserDto userDto, @NotNull @PathVariable Long id) {
        log.info("PATCH-request: update user={} with id={}", userDto, id);
        return userService.updateUserById(userDto, id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@NotNull @PathVariable Long id) {
        log.info("DELETE-request: delete a user with id={}", id);
        userService.deleteUserById(id);
    }
}
