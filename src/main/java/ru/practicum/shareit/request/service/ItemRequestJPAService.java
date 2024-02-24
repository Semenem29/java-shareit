package ru.practicum.shareit.request.service;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestJPAService {
    ItemRequestResponseDto createRequest(Long requesterId, ItemRequestDto itemRequestDto);

    List<ItemRequestResponseDto> findAllByRequester(Long requesterId);

    ItemRequestResponseDto getRequestById(Long requesterId, Long requestId);

    List<ItemRequestResponseDto> getAllOtherRequests(Long requesterId, Integer from, Integer size);
}
