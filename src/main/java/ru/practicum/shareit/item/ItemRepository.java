package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {
    Item createItem(Item item);

    Item updateItem(Item item, Long itemId);

    Item getItemById(Long itemId);

    Collection<Item> getAllItems();
}
