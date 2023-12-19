package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {

    Collection<User> getAllUsers();

    User createUser(User user);

    User getUserById(Long id);

    User updateUserById(User user, Long id);

    User deleteUserById(Long id);
}
