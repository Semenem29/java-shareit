package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserAlreadyExistException;
import ru.practicum.shareit.user.dto.UserDto;
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
    public Collection<UserDto> getAllUsers() {
        return userRepository.getAllUsers().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public User createUser(User user) {
        emailAlreadyExistValidation(user);
        return userRepository.createUser(user);
    }

    @Override
    public UserDto getUserById(Long id) {
        return UserMapper.toUserDto(userRepository.getUserById(id));
    }

    @Override
    public User updateUserById(User user, Long id) {
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
        return userRepository.deleteUserById(id);
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
