package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.UserAlreadyExistException;
import ru.practicum.shareit.exception.UserNotExistException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserServiceIntegrationTest {

    @Autowired
    UserJPAService userService;

    @Test
    public void shouldCreateUser_andGetByExistingId() {

        UserDto userDto = UserDto.builder()
                .name("Jordan")
                .email("mj32@gmail.com")
                .build();

        User user = UserMapper.toUser(userDto);

        Long userId = 1L;
        User savedUser = user.toBuilder()
                .id(userId)
                .build();

        UserDto expectedUserDto = UserMapper.toUserDto(savedUser);

        UserDto result = userService.createUser(userDto);

        assertEquals(result, expectedUserDto);
    }

    @Test
    public void shouldGetUserByExistingId() {

        UserDto userDto = UserDto.builder()
                .name("Jordan")
                .email("mj32@gmail.com")
                .build();

        User user = UserMapper.toUser(userDto);

        Long userId = 1L;
        User savedUser = user.toBuilder()
                .id(userId)
                .build();

        UserDto expectedUserDto = UserMapper.toUserDto(savedUser);

        userService.createUser(userDto);

        UserDto result = userService.getUserById(userId);

        assertEquals(result, expectedUserDto);
    }

    @Test
    public void shouldThrowExceptionIfUserNotFound() {

        Long userId = 1L;

        assertThrows(UserNotExistException.class,
                () -> userService.getUserById(userId),
                String.format("there is no user with id: %d", userId));
    }

    @Test
    public void shouldUpdateValidUserFields() {

        Long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("Jordan")
                .email("mj32@gmail.com")
                .build();

        UserDto savedUserDto = userService.createUser(userDto);
        User savedUser = UserMapper.toUser(savedUserDto);

        UserDto validNew = UserDto.builder()
                .id(3L)
                .name("NewName")
                .email("New@Email")
                .build();

        User updatedUser = savedUser.toBuilder()
                .name(validNew.getName())
                .email(validNew.getEmail())
                .build();

        UserDto expectedUserDto = UserMapper.toUserDto(updatedUser);

        UserDto result = userService.updateUserById(validNew, userId);

        assertEquals(result, expectedUserDto);
        assertThat(expectedUserDto)
                .hasFieldOrPropertyWithValue("id", userId)
                .hasFieldOrPropertyWithValue("name", "NewName")
                .hasFieldOrPropertyWithValue("email", "New@Email");
    }

    @Test
    public void shouldFailUpdateIfUserNotFound() {

        Long userId = 1L;

        UserDto validNew = UserDto.builder()
                .id(3L)
                .name("NewName")
                .email("New@Email")
                .build();

        assertThrows(UserNotExistException.class,
                () -> userService.getUserById(userId),
                String.format("there is no user with id: %d", userId));
    }

    @Test
    public void shouldUpdateOnlyUserName() {

        UserDto validNew = UserDto.builder()
                .id(3L)
                .name("NewName")
                .build();

        Long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("Jordan")
                .email("mj32@gmail.com")
                .build();

        UserDto savedUserDto = userService.createUser(userDto);
        User savedUser = UserMapper.toUser(savedUserDto);

        User updatedUser = savedUser.toBuilder()
                .name(validNew.getName())
                .build();

        UserDto expectedUserDto = UserMapper.toUserDto(updatedUser);

        UserDto result = userService.updateUserById(validNew, userId);

        assertEquals(result, expectedUserDto);
        assertThat(expectedUserDto)
                .hasFieldOrPropertyWithValue("id", userId)
                .hasFieldOrPropertyWithValue("name", "NewName")
                .hasFieldOrPropertyWithValue("email", "mj32@gmail.com");
    }

    @Test
    public void shouldUpdateOnlyUserEmail() {

        UserDto validNew = UserDto.builder()
                .id(3L)
                .email("new@mail.ru")
                .build();

        Long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("Jordan")
                .email("mj32@gmail.com")
                .build();

        UserDto savedUserDto = userService.createUser(userDto);
        User savedUser = UserMapper.toUser(savedUserDto);

        User updatedUser = savedUser.toBuilder()
                .email(validNew.getEmail())
                .build();

        UserDto expectedUserDto = UserMapper.toUserDto(updatedUser);

        UserDto result = userService.updateUserById(validNew, userId);

        assertEquals(result, expectedUserDto);
        assertThat(expectedUserDto)
                .hasFieldOrPropertyWithValue("id", userId)
                .hasFieldOrPropertyWithValue("name", "Jordan")
                .hasFieldOrPropertyWithValue("email", "new@mail.ru");
    }

    @Test
    public void shouldFailUpdateWithOtherUserEmail() {

        UserDto userDto = UserDto.builder()
                .name("Defor")
                .email("fedja@yandex.ru")
                .build();
        Long userId2 = 2L;
        UserDto userWithSameEmailDto = UserDto.builder()
                .name("Jordan")
                .email("mj32@gmail.com")
                .build();

        userService.createUser(userDto);
        userService.createUser(userWithSameEmailDto);

        UserDto invalidNew = UserDto.builder()
                .id(3L)
                .email("fedja@yandex.ru")
                .build();

        assertThrows(UserAlreadyExistException.class,
                () -> userService.updateUserById(invalidNew, userId2),
                String.format("user is ALREADY exist with email: %s", invalidNew.getEmail()));
    }

    @Test
    public void shouldDeleteUser() {

        Long userId = 1L;
        UserDto user = UserDto.builder()
                .name("Jordan")
                .email("mj32@gmail.com")
                .build();

        userService.createUser(user);

        userService.deleteUserById(userId);

        assertThrows(UserNotExistException.class,
                () -> userService.getUserById(userId),
                String.format("there is no user with id: %s", userId));
    }

    @Test
    public void shouldFindAllUsers() {

        Long user1Id = 1L;
        UserDto userDto1 = UserDto.builder()
                .name("Jordan")
                .email("mj32@gmail.com")
                .build();
        UserDto savedUser1 = userService.createUser(userDto1);
        Long user2Id = 1L;
        UserDto userDto2 = UserDto.builder()
                .name("Oleg")
                .email("tinkoff@google.com")
                .build();
        UserDto savedUser2 = userService.createUser(userDto2);

        List<UserDto> users = List.of(savedUser1, savedUser2);

        List<UserDto> result = new ArrayList<>(userService.getAllUsers());

        assertEquals(result, users);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getId(), user1Id);
        assertEquals(result.get(0).getName(), "Jordan");
        assertEquals(result.get(0).getEmail(), "mj32@gmail.com");
        assertEquals(result.get(0).getId(), user2Id);
        assertEquals(result.get(1).getName(), "Oleg");
        assertEquals(result.get(1).getEmail(), "tinkoff@google.com");
    }

}
