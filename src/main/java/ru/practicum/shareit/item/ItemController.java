package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemJPAService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemJPAService itemService;

    @PostMapping
    public ItemDto createItem(@NotNull @RequestHeader("X-Sharer-User-Id") Long ownerId,
                              @NotNull @Valid @RequestBody ItemDto itemDto) {
        log.info("POST-request: create item request from user with id= {}, item: {}", ownerId, itemDto);
        return itemService.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@NotNull @RequestHeader("X-Sharer-User-Id") Long ownerId,
                              @NotNull @RequestBody ItemDto itemDto,
                              @NotNull @PathVariable Long itemId) {
        log.info("PATCH-request: update item with id={} from user with id= {}, item: {}", itemId, ownerId, itemDto);
        return itemService.updateItem(itemDto, ownerId, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getItemById(@NotNull @RequestHeader("X-Sharer-User-Id") Long userId,
                                       @NotNull @PathVariable Long itemId) {
        log.info("GET-request: get item request from user with id={} and itemId={}", userId, itemId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemResponseDto> getItemsOfOwner(@NotNull @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("GET-request: get items of owner request from user with id={}", ownerId);
        return itemService.getItemsOfOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> searchItems(@NotNull @RequestParam String text) {
        log.info("GET-request: find items by substring={} in the name or the description", text);
        return itemService.findItemsByText(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(@NotNull @RequestHeader("X-Sharer-User-Id") Long userId,
                                         @NotNull @PathVariable Long itemId,
                                         @RequestBody @Valid CommentRequestDto commentRequestDto) {
        log.info("POST-request: from user with id={}, about item with id={}, add to comment: {}",
                userId,
                itemId,
                commentRequestDto);
        return itemService.addComment(userId, itemId, commentRequestDto);
    }

}
