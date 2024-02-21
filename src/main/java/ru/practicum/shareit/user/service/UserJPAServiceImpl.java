package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserAlreadyExistException;
import ru.practicum.shareit.exception.UserNotExistException;
import ru.practicum.shareit.user.storage.UserJPARepository;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserJPAServiceImpl implements UserJPAService {

    private final UserJPARepository userJPARepository;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        userJPARepository.save(user);
        userDto = UserMapper.toUserDto(user);
        log.info("Created USER={}", userDto);
        return userDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<UserDto> getAllUsers() {
        List<User> users = userJPARepository.findAll();
        List<UserDto> usersDto = UserMapper.toUserDtoList(users);
        logUsersList(usersDto);
        return usersDto;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        User fetchedUser = getUserOrThrow(userId);
        UserDto userDto = UserMapper.toUserDto(fetchedUser);
        log.info("provided info about user with id={}: {}", userId, userDto);
        return userDto;
    }

    @Override
    @Transactional
    public UserDto updateUserById(UserDto userDto, Long userId) {

        User fetchedUser = getUserOrThrow(userId);
        String newName = userDto.getName();
        String newEmail = userDto.getEmail();
        if (newName != null && !newName.isBlank()) {
            fetchedUser = fetchedUser.toBuilder().name(newName).build();
        }
        if (newEmail != null && !newEmail.isBlank()) {
            if (!Objects.equals(fetchedUser.getEmail(), userDto.getEmail())) {
                emailAlreadyExistValidation(userDto.getId(), newEmail);
                fetchedUser = fetchedUser.toBuilder().email(newEmail).build();
            }
        }

        userJPARepository.save(fetchedUser);
        log.info("USER with id={} was UPDATED: {}", fetchedUser.getId(), fetchedUser);

        return UserMapper.toUserDto(fetchedUser);
    }

    @Override
    @Transactional
    public void deleteUserById(Long userId) {
        User fetchedUser = getUserOrThrow(userId);
        userJPARepository.delete(fetchedUser);
        log.info("user with id={} was deleted", userId);
    }

    private void emailAlreadyExistValidation(Long userId, String email) {
        List<User> storedUser = userJPARepository.findAll().stream()
                .filter(user1 -> !user1.getId().equals(userId) && user1.getEmail().equals(email))
                .collect(Collectors.toList());

        if (storedUser.size() > 0) {
            String message = "user is ALREADY exist with email: " + email;
            log.error("UserAlreadyExistException: " + message);
            throw new UserAlreadyExistException(message);
        }
    }

    private User getUserOrThrow(Long userId) {
        Optional<User> fetchedUserOpt = userJPARepository.findById(userId);
        if (fetchedUserOpt.isEmpty()) {
            String message = "there is no user with id: " + userId;
            log.error("UserNotExistException: " + message);
            throw new UserNotExistException(message);
        }

        return fetchedUserOpt.get();
    }

    private void logUsersList(List<UserDto> users) {
        String result = users.stream()
                .map(UserDto::toString)
                .collect(Collectors.joining(", "));
        log.info("Provided list of users: {}", result);
    }
}
