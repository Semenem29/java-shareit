package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserAlreadyExistException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    @Autowired
    private final UserRepository userRepository;

    @Override
    public Collection<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public User createUser(User user) {
        if (user == null) {
            throw new ValidationException("user cannot be null");
        }

        userCreateValidation(user);
        return userRepository.createUser(user);
    }

    @Override
    public User getUserById(Long id) {
        if (id == null) {
            throw new ValidationException("userId cannot be null");
        }

        return userRepository.getUserById(id);
    }

    @Override
    public User updateUserById(User user, Long id) {
        if (id == null) {
            throw new ValidationException("provided data cannot be null");
        }

        User fetchedUser = userRepository.getUserById(id);
        user.setId(fetchedUser.getId());

        if (user.getName() == null) {
            user.setName(fetchedUser.getName());
        }

        if (user.getEmail() == null) {
            user.setEmail(fetchedUser.getEmail());
        } else {
            emailAlreadyExistValidation(user);
        }

        return userRepository.updateUserById(user, id);
    }

    @Override
    public User deleteUserById(Long id) {
        if (id == null) {
            throw new ValidationException("userId cannot be null");
        }

        return userRepository.deleteUserById(id);
    }

    private void userCreateValidation(User user) {
        if (user == null) {
            throw new ValidationException("user cannot be null");
        }

        emailAndNameValidation(user.getEmail(), user.getName());
        emailAlreadyExistValidation(user);
    }

    private void emailAndNameValidation(String email, String name) {
        if (email == null || email.isBlank() || !email.contains("@") || !email.contains(".")) {
            throw new ValidationException("invalid or empty email was provided!");
        }

        if (name == null || name.isBlank()) {
            throw new ValidationException("invalid or empty name was provided!");
        }
    }

    private void emailAlreadyExistValidation(User user) {
        Long userId = user.getId();
        List<User> storedUser = userRepository.getAllUsers().stream()
                .filter(user1 -> !user1.getId().equals(userId) && user1.getEmail().equals(user.getEmail()))
                .collect(Collectors.toList());

        if (storedUser.size() > 0) {
            throw new UserAlreadyExistException("user is ALREADY exist with email: " + user.getEmail());
        }
    }
}
