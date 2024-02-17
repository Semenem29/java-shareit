package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {
    Item createItem(ItemDto itemDto, Long ownerId);

    Item updateItem(ItemDto itemDto, Long ownerId, Long itemId);

    ItemDto getItemById(Long itemId);

    Collection<ItemDto> getItemsOfOwner(Long ownerId);

    Collection<ItemDto> findItemsByText(String text);
}
