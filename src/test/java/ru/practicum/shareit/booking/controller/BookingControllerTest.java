package ru.practicum.shareit.booking.controller;

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
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingJPAService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc
public class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingJPAService bookingJPAService;

    String header;
    MediaType jsonType;
    Long userId;

    @BeforeEach
    void init() {
        userId = 1L;
        header = "X-Sharer-User-Id";
        jsonType = MediaType.APPLICATION_JSON;
    }

    @Test
    @SneakyThrows
    public void createBooking_WhenBookingIsValid_StatusIsOk_AndInvokeService() {
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = LocalDateTime.of(2030, 2, 1, 1, 1, 1);

        User user = User.builder()
                .id(userId)
                .name("John")
                .build();

        Item item = Item.builder()
                .name("spoon")
                .build();

        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .build();

        BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(
                BookingMapper.toBooking(bookingDto, user, item, BookingStatus.WAITING));

        String bookingString = objectMapper.writeValueAsString(bookingDto);
        String expectedBookingString = objectMapper.writeValueAsString(bookingResponseDto);

        when(bookingJPAService.createBooking(bookingDto, userId))
                .thenReturn(bookingResponseDto);

        String result = mockMvc.perform(post("/bookings")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(bookingString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.start", is(bookingResponseDto.getStart().toString()), LocalDateTime.class))
                .andExpect(jsonPath("$.end", is(bookingResponseDto.getEnd().toString()), LocalDateTime.class))
                .andExpect(jsonPath("$.item", is(bookingResponseDto.getItem()), Item.class))
                .andExpect(jsonPath("$.booker", is(bookingResponseDto.getBooker()), User.class))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString()), BookingStatus.class))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingJPAService, times(1)).createBooking(bookingDto, userId);

        assertThat(expectedBookingString, is(result));
    }

    @Test
    @SneakyThrows
    public void createBooking_WhenBookingHasEndInPast_StatusIsBadRequest_NotInvokeService() {

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime pastEnd = LocalDateTime.of(2007, 2, 1, 1, 1, 1);

        BookingDto invalidBookingDto = BookingDto.builder()
                .start(start)
                .end(pastEnd)
                .build();

        String invalidBookingString = objectMapper.writeValueAsString(invalidBookingDto);

        mockMvc.perform(post("/bookings")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(invalidBookingString))
                .andExpect(status().isBadRequest());

        verify(bookingJPAService, Mockito.never()).createBooking(invalidBookingDto, userId);
    }

    @Test
    @SneakyThrows
    public void createBooking_WhenBookingHasStartNull_StatusIsBadRequest_NotInvokeService() {
        LocalDateTime startNull = null;
        LocalDateTime end = LocalDateTime.of(2030, 2, 1, 1, 1, 1);

        BookingDto invalidBookingDto = BookingDto.builder()
                .start(startNull)
                .end(end)
                .build();

        String invalidBookingString = objectMapper.writeValueAsString(invalidBookingDto);

        mockMvc.perform(post("/bookings")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(invalidBookingString))
                .andExpect(status().isBadRequest());

        verify(bookingJPAService, Mockito.never()).createBooking(invalidBookingDto, userId);
    }

    @Test
    @SneakyThrows
    public void createBooking_WhenBookingHasEndNull_StatusIsBadRequest_NotInvokeService() {
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime endNull = null;

        BookingDto invalidBookingDto = BookingDto.builder()
                .start(start)
                .end(endNull)
                .build();

        String invalidBookingString = objectMapper.writeValueAsString(invalidBookingDto);

        mockMvc.perform(post("/bookings")
                        .header(header, userId)
                        .contentType(jsonType)
                        .content(invalidBookingString))
                .andExpect(status().isBadRequest());

        verify(bookingJPAService, Mockito.never()).createBooking(invalidBookingDto, userId);
    }

    @Test
    @SneakyThrows
    public void approveBookingStatus_StatusIsOk_InvokeService() {
        String approved = "approved";
        String approvedValue = "true";

        User user = User.builder()
                .id(userId)
                .name("John")
                .email("john_tinkoff@gmail.com")
                .build();

        Item item = Item.builder()
                .name("spoon")
                .build();

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = LocalDateTime.of(2030, 2, 1, 1, 1, 1);

        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .build();

        BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(
                BookingMapper.toBooking(bookingDto, user, item, BookingStatus.WAITING));

        System.out.println("id?: " + bookingResponseDto.getId());

        String bookingString = objectMapper.writeValueAsString(bookingDto);
        String expectedBookingString = objectMapper.writeValueAsString(bookingResponseDto);

        when(bookingJPAService.approveBookingStatus(true, bookingId, userId))
                .thenReturn(bookingResponseDto);

        String result = mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(header, userId)
                        .contentType(jsonType)
                        .param(approved, approvedValue)
                        .content(bookingString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingResponseDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingResponseDto.getEnd().toString())))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString())))
                .andExpect(jsonPath("$.item.name", is("spoon")))
                .andExpect(jsonPath("$.booker.name", is("John")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingJPAService, Mockito.times(1))
                .approveBookingStatus(true, bookingId, userId);

        assertThat(result, is(expectedBookingString));


    }

    @Test
    @SneakyThrows
    public void approveBookingStatus_WhenParamHasInvalidName_StatusIsInternalServerError_NotIvokeService() {

        String invalidApproved = "notValidApproved";
        String approvedValue = "true";

        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .id(bookingId)
                .build();

        String bookingString = objectMapper.writeValueAsString(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(header, userId)
                        .param(invalidApproved, approvedValue)
                        .contentType(jsonType)
                        .content(bookingString))
                .andExpect(status().isInternalServerError());

        verify(bookingJPAService, never()).approveBookingStatus(anyBoolean(), anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    public void approveBookingStatus_WhenParamHasInvalidValue_StatusIsInternalServerError_NotInvokeService() {
        String approved = "approved";
        String invalidApprovedValue = "invalidApprovedValue";

        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .id(bookingId)
                .build();

        String bookingString = objectMapper.writeValueAsString(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(header, userId)
                        .param(approved, invalidApprovedValue)
                        .contentType(jsonType)
                        .content(bookingString))
                .andExpect(status().isInternalServerError());

        verify(bookingJPAService, never()).approveBookingStatus(anyBoolean(), anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    public void getBooking_StatusIsOk_InvokeService() {
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = LocalDateTime.of(2030, 2, 1, 1, 1, 1);

        User user = User.builder()
                .id(userId)
                .name("John")
                .email("john_tinkoff@gmail.com")
                .build();

        Long itemId = 1L;
        Item item = Item.builder()
                .id(itemId)
                .name("spoon")
                .build();

        Long bookingId = 1L;
        BookingDto bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();

        BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(
                BookingMapper.toBooking(bookingDto, user, item, BookingStatus.WAITING));

        String expectedBookingResponseString = objectMapper.writeValueAsString(bookingResponseDto);

        when(bookingJPAService.getBooking(bookingId, userId))
                .thenReturn(bookingResponseDto);

        String result = mockMvc.perform(get("/bookings/{id}", bookingId)
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonType))
                .andExpect(content().json(expectedBookingResponseString))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingJPAService, Mockito.times(1)).getBooking(bookingId, userId);

        assertThat(result, is(expectedBookingResponseString));
    }

    @Test
    @SneakyThrows
    public void getByOwner_WhenParametersAreValid_IsStatusOk_InvokeService() {
        String state = "state";
        String stateValue = "APPROVED";
        String from = "from";
        String fromValue = "0";
        String size = "size";
        String sizeValue = "10";

        Long bookingId1 = 1L;
        Long bookingId2 = 2L;

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = LocalDateTime.of(2030, 2, 1, 1, 1, 1);

        BookingResponseDto bookingDto1 = BookingResponseDto.builder()
                .id(bookingId1)
                .start(start)
                .end(end)
                .status(BookingStatus.APPROVED)
                .build();
        BookingResponseDto bookingDto2 = BookingResponseDto.builder()
                .id(bookingId2)
                .start(start.plusHours(3L))
                .end(end.plusDays(1))
                .status(BookingStatus.WAITING)
                .build();

        List<BookingResponseDto> bookingResponseDtoList = List.of(bookingDto1, bookingDto2);

        String expectedBookingListString = objectMapper.writeValueAsString(bookingResponseDtoList);

        when(bookingJPAService.getBookingsByOwner(userId, "APPROVED", 0, 10))
                .thenReturn(bookingResponseDtoList);

        String result = mockMvc.perform(get("/bookings/owner")
                        .header(header, userId)
                        .param(state, stateValue)
                        .param(from, fromValue)
                        .param(size, sizeValue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].id", is(bookingDto1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", is(bookingDto1.getStart().toString())))
                .andExpect(jsonPath("$.[0].end", is(bookingDto1.getEnd().toString())))
                .andExpect(jsonPath("$.[0].status", is(bookingDto1.getStatus().toString())))
                .andExpect(jsonPath("$.[1].id", is(bookingDto2.getId()), Long.class))
                .andExpect(jsonPath("$.[1].start", is(bookingDto2.getStart().toString())))
                .andExpect(jsonPath("$.[1].end", is(bookingDto2.getEnd().toString())))
                .andExpect(jsonPath("$.[1].status", is(bookingDto2.getStatus().toString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingJPAService)
                .getBookingsByOwner(userId, stateValue, Integer.valueOf(fromValue), Integer.valueOf(sizeValue));

        assertThat(result, is(expectedBookingListString));
    }

    @Test
    @SneakyThrows
    public void getByOwner_WhenFromIsNegative_IsStatusBadRequest_NotInvokeService() {

        String invalidFromValue = "-1";

        String state = "state";
        String stateValue = "APPROVED";
        String from = "from";
        String size = "size";
        String sizeValue = "10";

        mockMvc.perform(get("/bookings/owner")
                        .header(header, userId)
                        .param(state, stateValue)
                        .param(from, invalidFromValue)
                        .param(size, sizeValue))
                .andExpect(status().isBadRequest());

        verify(bookingJPAService, never()).getBookingsByOwner(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    public void getByOwner_WhenFromIsString_IsStatusInternalServerError_NotInvokeService() {
        String invalidFromValue = "invalidFromValue";

        String state = "state";
        String stateValue = "APPROVED";
        String from = "from";
        String size = "size";
        String sizeValue = "10";

        mockMvc.perform(get("/bookings/owner")
                        .header(header, userId)
                        .param(state, stateValue)
                        .param(from, invalidFromValue)
                        .param(size, sizeValue))
                .andExpect(status().isInternalServerError());

        verify(bookingJPAService, never()).getBookingsByOwner(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    public void getByOwner_WhenSizeIsNegative_IsStatusBadRequest_NotInvokeService() {

        String invalidSizeValue = "-10";

        String state = "state";
        String stateValue = "APPROVED";
        String from = "from";
        String fromValue = "0";
        String size = "size";

        mockMvc.perform(get("/bookings/owner")
                        .header(header, userId)
                        .param(state, stateValue)
                        .param(from, fromValue)
                        .param(size, invalidSizeValue))
                .andExpect(status().isBadRequest());

        verify(bookingJPAService, never()).getBookingsByOwner(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    public void getByOwner_WhenSizeIsZero_IsStatusBadRequest_NotInvokeService() {

        String invalidSizeValue = "0";

        String state = "state";
        String stateValue = "APPROVED";
        String from = "from";
        String fromValue = "1";
        String size = "size";


        mockMvc.perform(get("/bookings/owner")
                        .header(header, userId)
                        .param(state, stateValue)
                        .param(from, fromValue)
                        .param(size, invalidSizeValue))
                .andExpect(status().isBadRequest());

        verify(bookingJPAService, never()).getBookingsByOwner(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    public void getByOwner_WhenSizeIsString_IsStatusInternalServerError_NotInvokeService() {
        String invalidSizeValue = "invalidSizeValue";

        String state = "state";
        String stateValue = "APPROVED";
        String from = "from";
        String fromValue = "1";
        String size = "size";


        mockMvc.perform(get("/bookings/owner")
                        .header(header, userId)
                        .param(state, stateValue)
                        .param(from, fromValue)
                        .param(size, invalidSizeValue))
                .andExpect(status().isInternalServerError());

        verify(bookingJPAService, never()).getBookingsByOwner(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    public void getByBooker_WhenParametersAreValid_IsStatusOk_InvokeService() {

        String state = "state";
        String stateValue = "APPROVED";
        String from = "from";
        String fromValue = "0";
        String size = "size";
        String sizeValue = "10";

        Long bookingId1 = 1L;
        Long bookingId2 = 2L;

        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = LocalDateTime.of(2030, 2, 1, 1, 1, 1);

        BookingResponseDto bookingDto1 = BookingResponseDto.builder()
                .id(bookingId1)
                .start(start)
                .end(end)
                .status(BookingStatus.APPROVED)
                .build();
        BookingResponseDto bookingDto2 = BookingResponseDto.builder()
                .id(bookingId2)
                .start(start.plusHours(3L))
                .end(end.plusDays(1))
                .status(BookingStatus.WAITING)
                .build();

        List<BookingResponseDto> bookingResponseDtoList = List.of(bookingDto1, bookingDto2);

        String expectedBookingsListString = objectMapper.writeValueAsString(bookingResponseDtoList);

        when(bookingJPAService.getBookingsByBooker(userId, "APPROVED", 0, 10))
                .thenReturn(bookingResponseDtoList);

        String result = mockMvc.perform(get("/bookings")
                        .header(header, userId)
                        .param(state, stateValue)
                        .param(from, fromValue)
                        .param(size, sizeValue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].id", is(bookingDto1.getId()), Long.class))
                .andExpect(jsonPath("$.[0].start", is(bookingDto1.getStart().toString())))
                .andExpect(jsonPath("$.[0].end", is(bookingDto1.getEnd().toString())))
                .andExpect(jsonPath("$.[0].status", is(bookingDto1.getStatus().toString())))
                .andExpect(jsonPath("$.[1].id", is(bookingDto2.getId()), Long.class))
                .andExpect(jsonPath("$.[1].start", is(bookingDto2.getStart().toString())))
                .andExpect(jsonPath("$.[1].end", is(bookingDto2.getEnd().toString())))
                .andExpect(jsonPath("$.[1].status", is(bookingDto2.getStatus().toString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingJPAService)
                .getBookingsByBooker(userId, stateValue, Integer.valueOf(fromValue), Integer.valueOf(sizeValue));

        assertThat(result, is(expectedBookingsListString));
    }

    @Test
    @SneakyThrows
    public void getByBooker_WhenFromIsNegative_IsStatusBadRequest_NotInvokeService() {

        String invalidFromValue = "-1";

        String state = "state";
        String stateValue = "APPROVED";
        String from = "from";
        String size = "size";
        String sizeValue = "10";

        mockMvc.perform(get("/bookings")
                        .header(header, userId)
                        .param(state, stateValue)
                        .param(from, invalidFromValue)
                        .param(size, sizeValue))
                .andExpect(status().isBadRequest());

        verify(bookingJPAService, never()).getBookingsByBooker(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    public void getByBooker_WhenFromIsString_IsStatusInternalServerError_NotInvokeService() {
        String invalidFromValue = "invalidFromValue";

        String state = "state";
        String stateValue = "APPROVED";
        String from = "from";
        String size = "size";
        String sizeValue = "10";

        mockMvc.perform(get("/bookings")
                        .header(header, userId)
                        .param(state, stateValue)
                        .param(from, invalidFromValue)
                        .param(size, sizeValue))
                .andExpect(status().isInternalServerError());

        verify(bookingJPAService, never()).getBookingsByBooker(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    public void getByBooker_WhenSizeIsNegative_IsStatusBadRequest_NotInvokeService() {

        String invalidSizeValue = "-10";

        String state = "state";
        String stateValue = "APPROVED";
        String from = "from";
        String fromValue = "0";
        String size = "size";

        mockMvc.perform(get("/bookings")
                        .header(header, userId)
                        .param(state, stateValue)
                        .param(from, fromValue)
                        .param(size, invalidSizeValue))
                .andExpect(status().isBadRequest());

        verify(bookingJPAService, never()).getBookingsByBooker(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    public void getByBooker_WhenSizeIsZero_IsStatusBadRequest_NotInvokeService() {

        String invalidSizeValue = "0";

        String state = "state";
        String stateValue = "APPROVED";
        String from = "from";
        String fromValue = "1";
        String size = "size";


        mockMvc.perform(get("/bookings")
                        .header(header, userId)
                        .param(state, stateValue)
                        .param(from, fromValue)
                        .param(size, invalidSizeValue))
                .andExpect(status().isBadRequest());

        verify(bookingJPAService, never()).getBookingsByBooker(anyLong(), anyString(), anyInt(), anyInt());
    }

    @Test
    @SneakyThrows
    public void getByBooker_WhenSizeIsString_IsStatusInternalServerError_NotInvokeService() {
        String invalidSizeValue = "invalidSizeValue";

        String state = "state";
        String stateValue = "APPROVED";
        String from = "from";
        String fromValue = "1";
        String size = "size";


        mockMvc.perform(get("/bookings")
                        .header(header, userId)
                        .param(state, stateValue)
                        .param(from, fromValue)
                        .param(size, invalidSizeValue))
                .andExpect(status().isInternalServerError());

        verify(bookingJPAService, never()).getBookingsByBooker(anyLong(), anyString(), anyInt(), anyInt());
    }
}
