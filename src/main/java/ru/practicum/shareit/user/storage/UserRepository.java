package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {
    Collection<User> getAllUsers();

    User createUser(User user);

    User getUserById(Long id);

    User updateUserById(User user, Long id);

    User deleteUserById(Long id);
}
