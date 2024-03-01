package ru.practicum.shareit.item.controller;

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
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemJPAService;
import ru.practicum.shareit.item.ItemMapper;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
@AutoConfigureMockMvc
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemJPAService itemService;

    Long itemId;
    Long userId;
    Long requestId;
    Long commentId;
    String header;
    MediaType jsonType;

    @BeforeEach
    @SneakyThrows
    void init() {
        itemId = 1L;
        userId = 1L;
        requestId = 1L;
        header = "X-Sharer-User-Id";
        jsonType = MediaType.APPLICATION_JSON;
    }

    @Test
    @SneakyThrows
    public void create_WhenItemIsValid_StatusIsOk_andInvokeService() {

        ItemDto validItem = ItemDto.builder()
                .id(itemId)
                .name("a spoon")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();

        String expectedItemString = objectMapper.writeValueAsString(validItem);


        when(itemService.createItem(validItem, userId))
                .thenReturn(validItem);


        String result = mockMvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(expectedItemString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(validItem.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(validItem.getName())))
                .andExpect(jsonPath("$.description", is(validItem.getDescription())))
                .andExpect(jsonPath("$.available", is(validItem.getAvailable())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService).createItem(validItem, userId);

        assertEquals(expectedItemString, result);
    }

    @Test
    @SneakyThrows
    public void create_whenItemHasEmptyName_statusIsBadRequest_NotInvokeService() {

        String emptyName = "";

        ItemDto invalidItem = ItemDto.builder()
                .id(itemId)
                .name(emptyName)
                .description("description")
                .available(true)
                .requestId(1L)
                .build();

        String invalidItemString = objectMapper.writeValueAsString(invalidItem);

        mockMvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(invalidItemString))
                .andExpect(status().isBadRequest());

        verify(itemService, Mockito.never()).createItem(any(), anyLong());
    }

    @Test
    @SneakyThrows
    public void create_whenItemHasEmptyDescription_statusIsBadRequest_NotInvokeService() {

        String emptyDescription = "";

        ItemDto invalidItem = ItemDto.builder()
                .id(itemId)
                .name("a spoon")
                .description(emptyDescription)
                .available(true)
                .requestId(1L)
                .build();

        String invalidItemString = objectMapper.writeValueAsString(invalidItem);

        mockMvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(invalidItemString))
                .andExpect(status().isBadRequest());

        verify(itemService, Mockito.never()).createItem(any(), anyLong());
    }

    @Test
    @SneakyThrows
    public void create_whenItemHasNullName_statusIsBadRequest_NotInvokeService() {

        ItemDto invalidItem = ItemDto.builder()
                .id(itemId)
                .name(null)
                .description("description")
                .available(true)
                .requestId(1L)
                .build();

        String invalidItemString = objectMapper.writeValueAsString(invalidItem);

        mockMvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(invalidItemString))
                .andExpect(status().isBadRequest());

        verify(itemService, Mockito.never()).createItem(any(), anyLong());
    }

    @Test
    @SneakyThrows
    public void create_whenItemHasNullDescription_StatusIsBadRequest_NotInvokeService() {

        ItemDto invalidItem = ItemDto.builder()
                .id(itemId)
                .name("a spoon")
                .description(null)
                .available(true)
                .requestId(1L)
                .build();

        String invalidItemString = objectMapper.writeValueAsString(invalidItem);

        mockMvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(invalidItemString))
                .andExpect(status().isBadRequest());

        verify(itemService, Mockito.never()).createItem(any(), anyLong());
    }

    @Test
    @SneakyThrows
    public void create_whenItemHasNullAvailable_statusIsBadRequest_NotInvokeService() {

        ItemDto invalidItem = ItemDto.builder()
                .id(itemId)
                .name("a spoon")
                .description("description")
                .available(null)
                .requestId(1L)
                .build();

        String invalidItemString = objectMapper.writeValueAsString(invalidItem);

        mockMvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(invalidItemString))
                .andExpect(status().isBadRequest());

        verify(itemService, Mockito.never()).createItem(any(), anyLong());
    }

    @SneakyThrows
    @Test
    public void getById_statusIsOk_andInvokeService() {

        ItemDto itemDto = ItemDto.builder()
                .id(itemId)
                .name("Item")
                .description("description")
                .available(true)
                .requestId(1L)
                .build();
        ItemResponseDto item = ItemMapper.toItemResponseDto(itemDto);

        String expectedItemString = objectMapper.writeValueAsString(item);

        when(itemService.getItemById(userId, itemId))
                .thenReturn(item);

        String result = mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedItemString))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService).getItemById(userId, itemId);

        assertEquals(result, expectedItemString);
    }

    @Test
    @SneakyThrows
    public void update_statusIsOk_andInvokeService() {
        ItemDto validItem = ItemDto.builder()
                .id(itemId)
                .name("a spoon")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();

        String expectedItemString = objectMapper.writeValueAsString(validItem);

        when(itemService.updateItem(validItem, userId, itemId))
                .thenReturn(validItem);

        String result = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(expectedItemString))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService).updateItem(validItem, userId, itemId);

        assertEquals(expectedItemString, result);
    }

    @Test
    @SneakyThrows
    public void getListByUser_isStatusOk_andInvokeService() {
        int from = 0;
        int size = 10;

        ItemDto validItem1 = ItemDto.builder()
                .id(itemId)
                .name("a spoon")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();
        ItemDto validItem2 = ItemDto.builder()
                .id(itemId)
                .name("a spoon2")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();

        List<ItemDto> list = List.of(validItem1, validItem2);
        List<ItemResponseDto> items = list.stream()
                .map(ItemMapper::toItemResponseDto)
                .collect(Collectors.toList());

        String expectedItemsListString = objectMapper.writeValueAsString(items);

        when(itemService.getItemsOfOwner(userId, 0, 10))
                .thenReturn(items);

        String result = mockMvc.perform(get("/items")
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].id", is(validItem1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(validItem1.getName()), String.class))
                .andExpect(jsonPath("$.[1].name", is(validItem2.getName()), String.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService).getItemsOfOwner(userId, from, size);

        assertEquals(result, expectedItemsListString);
    }

    @Test
    @SneakyThrows
    public void searchItemsBySubstring_isStatusOk_andInvokeService() {
        int from = 0;
        int size = 10;

        String parameterName = "text";
        String parameterValue = "substring";

        ItemDto validItem1 = ItemDto.builder()
                .id(itemId)
                .name("a spoon")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();
        ItemDto validItem2 = ItemDto.builder()
                .id(itemId)
                .name("a spoon2")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();

        List<ItemDto> list = List.of(validItem1, validItem2);
        List<ItemResponseDto> items = list.stream()
                .map(ItemMapper::toItemResponseDto)
                .collect(Collectors.toList());

        String itemsString = objectMapper.writeValueAsString(items);

        when(itemService.findItemsByText(parameterValue, from, size))
                .thenReturn(items);

        String result = mockMvc.perform(get("/items/search")
                        .param(parameterName, parameterValue))
                .andExpect(status().isOk())
                .andExpect(content().json(itemsString))
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService).findItemsByText(parameterValue, from, size);

        assertEquals(result, itemsString);
    }

    @Test
    @SneakyThrows
    public void searchItemsBySubstring_whenParameterNameIsInvalid_isStatusInternalServerError_AndInvokeService() {
        int from = 0;
        int size = 10;

        String parameterName = "invalid";

        String parameterValue = "substring";

        ItemDto validItem1 = ItemDto.builder()
                .id(itemId)
                .name("a spoon")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();
        ItemDto validItem2 = ItemDto.builder()
                .id(itemId)
                .name("a spoon2")
                .description("description")
                .available(true)
                .requestId(requestId)
                .build();

        List<ItemDto> list = List.of(validItem1, validItem2);
        List<ItemResponseDto> items = list.stream()
                .map(ItemMapper::toItemResponseDto)
                .collect(Collectors.toList());

        when(itemService.findItemsByText(parameterValue, from, size))
                .thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param(parameterName, parameterValue))
                .andExpect(status().isInternalServerError());


        verify(itemService, never()).findItemsByText(anyString(), any(), any());
    }

    @Test
    @SneakyThrows
    public void addComment_whenValidComment_isStatusOk_andInvokeService() {

        CommentRequestDto comment = CommentRequestDto.builder()
                .id(commentId)
                .text("commentText")
                .authorName("Johny")
                .itemId(itemId)
                .build();
        CommentResponseDto commentResponseDto = CommentMapper.toCommentResponseDto(comment);

        String expectedCommentString = objectMapper.writeValueAsString(commentResponseDto);

        when(itemService.addComment(userId, itemId, comment))
                .thenReturn(commentResponseDto);

        String result = mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(header, userId)
                        .content(objectMapper.writeValueAsString(comment))
                        .contentType(jsonType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentResponseDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentResponseDto.getAuthorName())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(itemService).addComment(userId, itemId, comment);

        assertEquals(result, expectedCommentString);
    }

    @Test
    @SneakyThrows
    public void addComment_whenInvalidCommentEmptyText_isStatusBadRequest_NotInvokeService() {

        String emptyText = "";

        CommentRequestDto invalidComment = CommentRequestDto.builder()
                .id(commentId)
                .text(emptyText)
                .authorName("Peter")
                .itemId(itemId)
                .build();
        CommentResponseDto commentResponseDto = CommentMapper.toCommentResponseDto(invalidComment);

        when(itemService.addComment(userId, itemId, invalidComment))
                .thenReturn(commentResponseDto);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(header, userId)
                        .content(objectMapper.writeValueAsString(invalidComment))
                        .contentType(jsonType))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).addComment(userId, itemId, invalidComment);
    }

    @Test
    @SneakyThrows
    public void addComment_whenInvalidCommentNullText_isStatusBadRequest_NotInvokeService() {

        CommentRequestDto invalidComment = CommentRequestDto.builder()
                .id(commentId)
                .text(null)
                .authorName("Peter")
                .itemId(itemId)
                .build();
        CommentResponseDto commentResponseDto = CommentMapper.toCommentResponseDto(invalidComment);

        when(itemService.addComment(userId, itemId, invalidComment))
                .thenReturn(commentResponseDto);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(header, userId)
                        .content(objectMapper.writeValueAsString(invalidComment))
                        .contentType(jsonType))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).addComment(anyLong(), anyLong(), any());

    }


}
