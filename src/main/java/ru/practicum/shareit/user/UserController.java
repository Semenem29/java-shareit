package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.group.Create;
import ru.practicum.shareit.group.Update;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
@Validated
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public Collection<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@NotNull @PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PatchMapping("/{id}")
    @Validated(Update.class)
    public User updateUser(@NotNull @Valid @RequestBody User user, @NotNull @PathVariable Long id) {
        return userService.updateUserById(user, id);
    }

    @PostMapping
    @Validated(Create.class)
    public User createUser(@NotNull @Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    @DeleteMapping("/{id}")
    public User deleteUser(@NotNull @PathVariable Long id) {
        return userService.deleteUserById(id);
    }
}
