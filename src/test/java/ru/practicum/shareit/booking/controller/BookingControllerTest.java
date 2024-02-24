package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingJPAService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@WebMvcTest(controllers = BookingControllerTest.class)
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
    public void create_WhenBookingIsValid_StatusIsOk_AndInvokeService() {
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 1, 1, 1);
        LocalDateTime end = LocalDateTime.of(2030, 2, 1, 1, 1, 1);

        User user = User.builder()
                .id(userId)
                .name("John")
                .build();

        Item item = Item.builder()
                .name("spoon")
                .build();

        Booking

        BookingResponseDto bookingDto = BookingMapper.toBookingResponseDto(
                BookingMapper.toBooking(booking)
        )

    }
}
