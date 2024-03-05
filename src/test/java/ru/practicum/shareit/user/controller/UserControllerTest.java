package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserJPAService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserJPAService userService;

    Long userId;
    MediaType jsonType;

    @BeforeEach
    public void init() {
        userId = 1L;
        jsonType = MediaType.APPLICATION_JSON;
    }

    @Test
    @SneakyThrows
    public void createUser_WhenUserIsValid_StatusIsOk_InvokeService() {

        UserDto user = UserDto.builder()
                .id(userId)
                .name("Jorge")
                .email("clooney68@yandex.ru")
                .build();

        String userString = objectMapper.writeValueAsString(user);

        when(userService.createUser(user))
                .thenReturn(user);

        String result = mockMvc.perform(post("/users")
                        .contentType(jsonType)
                        .content(userString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService).createUser(user);

        assertThat(result, is(userString));
    }

    @Test
    @SneakyThrows
    public void createUser_WhenUserHasEmptyName_StatusIsBadRequest_NotInvokeService() {

        String emptyName = "";

        UserDto invalidUser = UserDto.builder()
                .id(userId)
                .name(emptyName)
                .email("clooney68@yandex.ru")
                .build();

        String invalidUserString = objectMapper.writeValueAsString(invalidUser);

        when(userService.createUser(invalidUser))
                .thenReturn(invalidUser);

        mockMvc.perform(post("/users")
                        .contentType(jsonType)
                        .content(invalidUserString))
                .andExpect(status().isBadRequest());

        verify(userService, Mockito.never()).createUser(any());
    }

    @Test
    @SneakyThrows
    public void createUser_WhenUserHasEmptyEmail_StatusIsBadRequest_NotInvokeService() {

        String emptyEmail = "";

        UserDto invalidUser = UserDto.builder()
                .id(userId)
                .name("Jorge")
                .email(emptyEmail)
                .build();

        String invalidUserString = objectMapper.writeValueAsString(invalidUser);

        when(userService.createUser(invalidUser))
                .thenReturn(invalidUser);

        mockMvc.perform(post("/users")
                        .contentType(jsonType)
                        .content(invalidUserString))
                .andExpect(status().isBadRequest());

        verify(userService, Mockito.never()).createUser(any());
    }

    @Test
    @SneakyThrows
    public void createUser_WhenUserHasNullName_StatusIsBadRequest_NotInvokeService() {

        UserDto invalidUser = UserDto.builder()
                .id(userId)
                .name(null)
                .email("clooney68@yandex.ru")
                .build();

        String invalidUserString = objectMapper.writeValueAsString(invalidUser);

        when(userService.createUser(invalidUser))
                .thenReturn(invalidUser);

        mockMvc.perform(post("/users")
                        .contentType(jsonType)
                        .content(invalidUserString))
                .andExpect(status().isBadRequest());

        verify(userService, Mockito.never()).createUser(any());
    }

    @Test
    @SneakyThrows
    public void createUser_WhenUserHasNullEmail_StatusIsBadRequest_NotInvokeService() {

        UserDto invalidUser = UserDto.builder()
                .id(userId)
                .name("Peter")
                .email(null)
                .build();

        String invalidUserString = objectMapper.writeValueAsString(invalidUser);

        when(userService.createUser(invalidUser))
                .thenReturn(invalidUser);

        mockMvc.perform(post("/users")
                        .contentType(jsonType)
                        .content(invalidUserString))
                .andExpect(status().isBadRequest());

        verify(userService, Mockito.never()).createUser(any());
    }

    @Test
    @SneakyThrows
    public void createUser_WhenUserHasInvalidEmail_StatusIsBadRequest_NotInvokeService() {

        String invalidEmail = "@321";

        UserDto invalidUser = UserDto.builder()
                .id(userId)
                .name("Peter")
                .email(invalidEmail)
                .build();

        String invalidUserString = objectMapper.writeValueAsString(invalidUser);

        when(userService.createUser(invalidUser))
                .thenReturn(invalidUser);

        mockMvc.perform(post("/users")
                        .contentType(jsonType)
                        .content(invalidUserString))
                .andExpect(status().isBadRequest());

        verify(userService, Mockito.never()).createUser(any());
    }

    @SneakyThrows
    @Test
    public void getUserById_StatusIsOk_InvokeService() {

        UserDto user = UserDto.builder()
                .id(userId)
                .name("Jorge")
                .email("clooney68@yandex.ru")
                .build();

        String userString = objectMapper.writeValueAsString(user);

        when(userService.getUserById(userId))
                .thenReturn(user);

        String result = mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonType))
                .andExpect(content().json(userString))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService).getUserById(userId);

        assertThat(result, is(userString));
    }

    @Test
    @SneakyThrows
    public void updateUser_WhenUserIsValid_StatusIsOk_InvokeService() {

        UserDto user = UserDto.builder()
                .id(userId)
                .name("Jorge")
                .email("clooney68@yandex.ru")
                .build();

        String userString = objectMapper.writeValueAsString(user);

        when(userService.updateUserById(user, userId))
                .thenReturn(user);

        String result = mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(jsonType)
                        .content(userString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService).updateUserById(user, userId);

        assertThat(result, is(userString));
    }

    @Test
    @SneakyThrows
    public void updateUser_WhenUserHasInvalidEmail_StatusIsBadRequest_NotInvokeService() {

        String invalidEmail = "@321";

        UserDto invalidUser = UserDto.builder()
                .id(userId)
                .name("Peter")
                .email(invalidEmail)
                .build();

        String invalidUserString = objectMapper.writeValueAsString(invalidUser);

        when(userService.updateUserById(invalidUser, userId))
                .thenReturn(invalidUser);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(jsonType)
                        .content(invalidUserString))
                .andExpect(status().isBadRequest());

        verify(userService, Mockito.never()).updateUserById(any(), anyLong());
    }

    @Test
    @SneakyThrows
    public void deleteUser_StatusIsOk_InvokeService() {

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userService).deleteUserById(userId);
    }

    @Test
    @SneakyThrows
    public void getAllUsers_StatusIsOk_InvokeService() {

        UserDto firstUser = UserDto.builder()
                .id(userId)
                .name("Brad")
                .email("mrsmith@gmail.com")
                .build();

        UserDto secondUser = UserDto.builder()
                .id(userId++)
                .name("Angelina")
                .email("mrssmith@gmail.com")
                .build();

        List<UserDto> users = List.of(firstUser, secondUser);

        String expectedUsersListAsString = objectMapper.writeValueAsString(users);

        when(userService.getAllUsers())
                .thenReturn(users);

        String result = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(userService).getAllUsers();

        assertThat(result, is(expectedUsersListAsString));
    }
}
