package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserJPAService {
    Collection<UserDto> getAllUsers();

    UserDto createUser(UserDto userDto);

    UserDto getUserById(Long id);

    UserDto updateUserById(UserDto userDto, Long id);

    void deleteUserById(Long id);
}
