package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.UserAlreadyExistException;
import ru.practicum.shareit.exception.UserNotExistException;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> userTable = new HashMap<>();
    private Long lastId = 0L;

    @Override
    public Collection<User> getAllUsers() {
        return userTable.values();
    }

    @Override
    public User createUser(User user) {
        if (userTable.containsKey(user.getId())) {
            throw new UserAlreadyExistException("user with id: " + user.getId() + " is ALREADY exist!");
        }

        user.setId(getLastId());
        userTable.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUserById(Long id) {
        return userTable.get(id);
    }

    @Override
    public User updateUserById(User user, Long id) {
        if (!userTable.containsKey(id)) {
            throw new UserNotExistException("user with id: " + id + " is NOT exist!");
        }

        user.setId(id);
        userTable.put(id, user);
        return user;
    }

    @Override
    public User deleteUserById(Long id) {
        if (!userTable.containsKey(id)) {
            throw new UserNotExistException("user with id: " + id + " is NOT exist!");
        }

        return userTable.remove(id);
    }

    private Long getLastId() {
        return ++lastId;
    }
}
