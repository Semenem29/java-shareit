package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestJPAService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
public class ItemRequestController {
    ItemRequestJPAService itemRequestJPAService;

    @PostMapping
    public ItemRequestResponseDto createRequest(@NotNull @RequestHeader("X-Sharer-User-Id") Long requesterId,
                                                @RequestBody @Valid ItemRequestDto itemRequestDto) {
        log.info("POST-request: user with id={} attemps to create the request with next itemRequest: \"{}\"",
                requesterId, itemRequestDto);
        return itemRequestJPAService.createRequest(requesterId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getMyRequests(@NotNull @RequestHeader("X-Sharer-User-Id") Long requesterId) {
        log.info("GET-request: the user with id={} request info about all his/her requests", requesterId);
        return itemRequestJPAService.findAllByRequester(requesterId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getRequestById(@NotNull @RequestHeader("X-Sharer-User-Id") Long requesterId,
                                                 @PathVariable @NotNull Long requestId) {
        log.info("GET-request: request info about request with id={}", requestId);
        return itemRequestJPAService.getRequestById(requesterId, requestId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAllOtherRequests(@NotNull @RequestHeader("X-Sharer-User-Id") Long requesterId,
                                                    @PositiveOrZero @RequestParam(
                                                            value = "from", defaultValue = "0") Integer from,
                                                    @Positive @RequestParam(
                                                            value = "size", defaultValue = "10") Integer size) {
        log.info("GET-request: reqquest info about other item requests from userId={}, with from={}, and size={}",
                requesterId, from, size);
        return itemRequestJPAService.getAllOtherRequests(requesterId, from, size);
    }

}
