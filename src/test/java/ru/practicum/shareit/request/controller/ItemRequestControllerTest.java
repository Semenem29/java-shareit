package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestJPAService;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = ItemRequestController.class)
@AutoConfigureMockMvc
public class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemRequestJPAService itemRequestService;
    Long itemRequestId;
    Long itemId;
    Long userId;
    Long requestId;
    String header;
    MediaType jsonType;

    @BeforeEach
    @SneakyThrows
    void init() {
        itemId = 1L;
        userId = 1L;
        requestId = 1L;
        itemRequestId = 1L;
        header = "X-Sharer-User-Id";
        jsonType = MediaType.APPLICATION_JSON;
    }

    @Test
    @SneakyThrows
    void createRequest_WhenItemRequestIsValid_StatusIsOk_InvokeService() {

        User user = User.builder()
                .name("Peter")
                .build();

        Item item = Item.builder()
                .name("spoon")
                .description("silver and new")
                .build();
        List<Item> items = List.of(item);

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("yo quiero una cuchara")
                .build();

        ItemRequest request = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        ItemRequestResponseDto itemRequestResponseDto = ItemRequestMapper
                .toItemRequestResponseDto(request, items)
                .toBuilder()
                .id(itemRequestId)
                .build();

        String validItemRequestString = objectMapper.writeValueAsString(itemRequestDto);
        String expectedItemRequestString = objectMapper.writeValueAsString(itemRequestResponseDto);

        when(itemRequestService.createRequest(userId, itemRequestDto))
                .thenReturn(itemRequestResponseDto);

        String result = mockMvc.perform(post("/requests")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(validItemRequestString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.created", is(itemRequestResponseDto.getCreated().toString())))
                .andExpect(jsonPath("$.description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$.items.[0].name", is("spoon")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemRequestService).createRequest(userId, itemRequestDto);
        assertThat(expectedItemRequestString, is(result));
    }

    @Test
    @SneakyThrows
    void createRequest_WhenItemRequestHasEmptyDescription_StatusIsBadRequest_NotInvokeService() {

        ItemRequestDto invalidItemRequest = ItemRequestDto.builder()
                .description("")
                .build();
        String invalidItemRequestString = objectMapper.writeValueAsString(invalidItemRequest);

        mockMvc.perform(post("/requests")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(invalidItemRequestString))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, Mockito.never()).createRequest(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void createRequest_WhenItemRequestHasNullDescription_StatusIsBadRequest_NotInvokeService() {

        ItemRequestDto invalidItemRequest = ItemRequestDto.builder()
                .description(null)
                .build();
        String invalidItemRequestString = objectMapper.writeValueAsString(invalidItemRequest);

        mockMvc.perform(post("/requests")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(invalidItemRequestString))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, Mockito.never()).createRequest(anyLong(), any());
    }

    @Test
    @SneakyThrows
    void getMyRequests_IsStatusOk_AndInvokeService() {

        User user = User.builder()
                .name("Jorge")
                .build();

        Item item = Item.builder()
                .name("spoon")
                .description("big and clean")
                .build();
        List<Item> items = List.of(item);

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("Im tired to eat rolltone with fork, I want to rent a spoon")
                .build();

        ItemRequest request = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        ItemRequestResponseDto itemRequestResponseDto = ItemRequestMapper
                .toItemRequestResponseDto(request, items)
                .toBuilder()
                .id(itemRequestId)
                .build();

        List<ItemRequestResponseDto> requests = List.of(itemRequestResponseDto);
        String expectedItemRequestString = objectMapper.writeValueAsString(requests);

        when(itemRequestService.findAllByRequester(userId)).thenReturn(requests);

        String result = mockMvc.perform(get("/requests")
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(itemRequestResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$.[0].created", is(itemRequestResponseDto.getCreated().toString())))
                .andExpect(jsonPath("$.[0].items.[0].name", is("spoon")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemRequestService).findAllByRequester(userId);
        assertThat(expectedItemRequestString, is(result));
    }

    @Test
    @SneakyThrows
    void getAllOtherRequests_WhenParametersAreValid_IsStatusOk_andInvokeService() {

        User user = User.builder()
                .name("Jorge")
                .build();

        Item item = Item.builder()
                .name("spoon")
                .description("big and clean")
                .build();
        List<Item> items = List.of(item);

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("Im tired to eat rolltone with fork, I want to rent a spoon")
                .build();

        ItemRequest request = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        ItemRequestResponseDto itemRequestResponseDto = ItemRequestMapper
                .toItemRequestResponseDto(request, items)
                .toBuilder()
                .id(itemRequestId)
                .build();

        List<ItemRequestResponseDto> requests = List.of(itemRequestResponseDto);
        String expectedItemRequestString = objectMapper.writeValueAsString(requests);

        String paramFromName = "from";
        Integer paramFromValue = 0;
        String paramSizeName = "size";
        Integer paramSizeValue = 10;

        when(itemRequestService.getAllOtherRequests(userId, paramFromValue, paramSizeValue))
                .thenReturn(requests);

        String result = mockMvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param(paramFromName, String.valueOf(paramFromValue))
                        .param(paramSizeName, String.valueOf(paramSizeValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(itemRequestResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$.[0].created", is(itemRequestResponseDto.getCreated().toString())))
                .andExpect(jsonPath("$.[0].items.[0].name", is("spoon")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemRequestService).getAllOtherRequests(userId, paramFromValue, paramSizeValue);
        assertThat(expectedItemRequestString, is(result));
    }

    @Test
    @SneakyThrows
    void getAllOtherRequests_WhenFromIsNegative_IsStatusBadRequest_NotInvokeService() {

        String paramFromName = "from";
        Integer paramFromValue = -1;
        String paramSizeName = "size";
        Integer paramSizeValue = 10;

        mockMvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param(paramFromName, String.valueOf(paramFromValue))
                        .param(paramSizeName, String.valueOf(paramSizeValue)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getAllOtherRequests(anyLong(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getAllOtherRequests_WhenSizeIsNegative_IsStatusBadRequest_NotInvokeService() {

        String paramFromName = "from";
        Integer paramFromValue = 0;
        String paramSizeName = "size";
        Integer paramSizeValue = -1;

        mockMvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param(paramFromName, String.valueOf(paramFromValue))
                        .param(paramSizeName, String.valueOf(paramSizeValue)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getAllOtherRequests(anyLong(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getAllOtherRequests_WhenSizeIsZero_IsStatusBadRequest_NotInvokeService() {

        String paramFromName = "from";
        Integer paramFromValue = 0;
        String paramSizeName = "size";
        Integer paramSizeValue = 0;

        mockMvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param(paramFromName, String.valueOf(paramFromValue))
                        .param(paramSizeName, String.valueOf(paramSizeValue)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).getAllOtherRequests(anyLong(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getAllOtherRequests_WhenFromIsNotNumber_IsStatusInternalServerError_NotInvokeService() {

        String paramFromName = "from";
        String paramFromValue = "from";
        String paramSizeName = "size";
        Integer paramSizeValue = 10;

        mockMvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param(paramFromName, paramFromValue)
                        .param(paramSizeName, String.valueOf(paramSizeValue)))
                .andExpect(status().isInternalServerError());

        verify(itemRequestService, never()).getAllOtherRequests(anyLong(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getAllOtherRequests_WhenSizeIsNotNumber_IsStatusInternalServerError_NotInvokeService() {

        String paramFromName = "from";
        String paramFromValue = "10";
        String paramSizeName = "size";
        String paramSizeValue = "size";

        mockMvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param(paramFromName, paramFromValue)
                        .param(paramSizeName, paramSizeValue))
                .andExpect(status().isInternalServerError());

        verify(itemRequestService, never()).getAllOtherRequests(anyLong(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    void getRequestById_StatusIsOk_InvokeService() {

        User user = User.builder()
                .name("Jorge")
                .build();

        Item item = Item.builder()
                .name("spoon")
                .description("big and clean")
                .build();
        List<Item> items = List.of(item);

        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("Im tired to eat rolltone with fork, I want to rent a spoon")
                .build();

        ItemRequest request = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        ItemRequestResponseDto itemRequestResponseDto = ItemRequestMapper
                .toItemRequestResponseDto(request, items)
                .toBuilder()
                .id(itemRequestId)
                .build();

        String expectedItemRequestString = objectMapper.writeValueAsString(itemRequestResponseDto);

        when(itemRequestService.getRequestById(userId, requestId))
                .thenReturn(itemRequestResponseDto);

        String result = mockMvc.perform(get("/requests/{requestId}", itemRequestId)
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonType))
                .andExpect(content().json(expectedItemRequestString))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemRequestService).getRequestById(userId, requestId);
        assertThat(expectedItemRequestString, is(result));
    }
}
