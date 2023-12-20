package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {

    Collection<UserDto> getAllUsers();

    User createUser(User user);

    UserDto getUserById(Long id);

    User updateUserById(User user, Long id);

    User deleteUserById(Long id);
}
