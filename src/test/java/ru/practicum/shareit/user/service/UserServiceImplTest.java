package ru.practicum.shareit.user.service;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.UserNotExistException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJPARepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @InjectMocks
    UserJPAServiceImpl userService;
    @Mock
    UserJPARepository userRepository;

    @Test
    public void createUser_returnSavedUser() {

        UserDto userDto = UserDto.builder()
                .name("Peter")
                .email("griffin@gmail.com")
                .build();

        User user = UserMapper.toUser(userDto);

        Long userId = 1L;
        User savedUser = user.toBuilder()
                .id(userId)
                .build();

        UserDto expectedUserDto = UserMapper.toUserDto(savedUser);

        when(userRepository.save(user))
                .thenReturn(savedUser);

        UserDto result = userService.createUser(userDto);

        verify(userRepository).save(user);

        assertThat(result, is(expectedUserDto));
    }

    @Test
    public void getUserById_WhenUserExist_ThenReturnUser() {

        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("Peter")
                .email("griffin@gmail.com")
                .build();

        UserDto expectedUserDto = UserMapper.toUserDto(user);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(userId);

        verify(userRepository).findById(userId);

        assertThat(result, is(expectedUserDto));
    }

    @Test
    public void getUserById_WhenUserNotFound_ThenThrowUserNotExist() {

        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class,
                () -> userService.getUserById(userId),
                String.format("there is no user with id: %d", userId));

        verify(userRepository).findById(userId);
    }

    @Test
    public void updateUser_WhenUserExists_WithValidFields_IgnoreIdField_ReturnUpdatedUser() {

        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("Peter")
                .email("griffin@gmail.com")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserDto validNew = UserDto.builder()
                .id(777L)
                .name("NewName")
                .email("New@Email")
                .build();

        User updatedUser = user.toBuilder()
                .name(validNew.getName())
                .email(validNew.getEmail())
                .build();

        UserDto expectedUserDto = UserMapper.toUserDto(updatedUser);

        UserDto result = userService.updateUserById(validNew, userId);

        verify(userRepository).save(updatedUser);

        assertThat(result, is(expectedUserDto));
        AssertionsForClassTypes.assertThat(expectedUserDto)
                .hasFieldOrPropertyWithValue("id", userId)
                .hasFieldOrPropertyWithValue("name", "NewName")
                .hasFieldOrPropertyWithValue("email", "New@Email");
    }

    @Test
    public void updateUser_WhenUserNotExist_ThenThrowUserNotExist() {

        Long userId = 1L;

        UserDto validNew = UserDto.builder()
                .id(3L)
                .name("NewName")
                .email("New@Email")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class,
                () -> userService.updateUserById(validNew, userId),
                String.format("there is no user with id: %d", userId));

        verify(userRepository, only()).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void updateUser_WhenUserExist_NameToUpdateIsValid_EmailIsNull_IgnoreIdField_ReturnUpdatedUser() {

        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("Peter")
                .email("griffin@gmail.com")
                .build();

        UserDto validNew = UserDto.builder()
                .id(3L)
                .name("NewName")
                .build();

        User updatedUser = user.toBuilder()
                .name(validNew.getName())
                .build();

        UserDto expectedUserDto = UserMapper.toUserDto(updatedUser);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserDto result = userService.updateUserById(validNew, userId);

        verify(userRepository).save(updatedUser);

        assertThat(result, is(expectedUserDto));
        AssertionsForClassTypes.assertThat(expectedUserDto)
                .hasFieldOrPropertyWithValue("id", userId)
                .hasFieldOrPropertyWithValue("name", "NewName")
                .hasFieldOrPropertyWithValue("email", "griffin@gmail.com");
    }

    @Test
    public void updateUser_WhenUserExists_NameIsNull_EmailIsValid_IgnoreIdField_ReturnUpdatedUser() {

        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("Peter")
                .email("griffin@gmail.com")
                .build();

        UserDto validNew = UserDto.builder()
                .id(3L)
                .email("new@mail.ru")
                .build();

        User updatedUser = user.toBuilder()
                .email(validNew.getEmail())
                .build();

        UserDto expectedUserDto = UserMapper.toUserDto(updatedUser);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        UserDto result = userService.updateUserById(validNew, userId);

        verify(userRepository).save(updatedUser);

        assertEquals(result, expectedUserDto);
        AssertionsForClassTypes.assertThat(expectedUserDto)
                .hasFieldOrPropertyWithValue("id", userId)
                .hasFieldOrPropertyWithValue("name", "Peter")
                .hasFieldOrPropertyWithValue("email", "new@mail.ru");
    }

    @Test
    public void deleteUser_WhenUserExists_InvokeRepository() {

        Long userId = 1L;
        User fetchedUser = User.builder()
                .id(userId)
                .name("Peter")
                .email("griffin@gmail.com")
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(fetchedUser));

        userService.deleteUserById(userId);

        verify(userRepository, times(1)).delete(fetchedUser);
    }

    @Test
    public void deleteUser_WhenUserNotExists_NotInvokeRepository() {

        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotExistException.class,
                () -> userService.deleteUserById(userId),
                String.format("there is no user with id: %d", userId));

        verify(userRepository, never()).deleteById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void getAllUsers_InvokeRepository_ReturnListOfUsers() {

        Long firstUserId = 1L;
        User user1 = User.builder()
                .id(firstUserId)
                .name("Peter")
                .email("griffin@gmail.com")
                .build();

        Long secondUesrId = 1L;
        User user2 = User.builder()
                .id(secondUesrId)
                .name("Dua")
                .email("Lipa@yandex.ru")
                .build();

        List<UserDto> users = UserMapper.toUserDtoList(List.of(user1, user2));

        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        List<UserDto> result = new ArrayList<>(userService.getAllUsers());

        verify(userRepository).findAll();

        assertEquals(result, users);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getName(), "Peter");
        assertEquals(result.get(1).getName(), "Dua");
    }

}
