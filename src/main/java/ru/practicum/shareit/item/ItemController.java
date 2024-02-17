package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.group.Create;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
@Validated
public class ItemController {

    @Autowired
    private final ItemService itemService;

    @PostMapping
    @Validated(Create.class)
    public Item createItem(@NotNull @RequestHeader("X-Sharer-User-Id") Long ownerId,
                           @NotNull @Valid @RequestBody ItemDto itemDto) {
        return itemService.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public Item updateItem(@NotNull @RequestHeader("X-Sharer-User-Id") Long ownerId,
                           @NotNull @RequestBody ItemDto itemDto,
                           @NotNull @PathVariable Long itemId) {
        return itemService.updateItem(itemDto, ownerId, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@NotNull @PathVariable Long itemId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public Collection<ItemDto> getItemsOfOwner(@NotNull @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.getItemsOfOwner(ownerId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItems(@NotNull @RequestParam String text) {
        return itemService.findItemsByText(text);
    }

}
