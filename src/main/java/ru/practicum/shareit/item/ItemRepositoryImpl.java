package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ItemAlreadyExistException;
import ru.practicum.shareit.exception.ItemNotExistException;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> itemTable = new HashMap<>();
    private Long lastId = 0L;

    @Override
    public Item createItem(Item item) {
        if (itemTable.containsKey(item.getId())) {
            throw new ItemAlreadyExistException("item with id: " + item.getId() + " is ALREADY exist!");
        }

        item.setId(getLastId());
        itemTable.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item, Long itemId) {
        if (!itemTable.containsKey(itemId)) {
            throw new ItemNotExistException("item with id: " + itemId + " is NOT exist!");
        }

        item.setId(itemId);
        itemTable.put(itemId, item);
        return item;
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemTable.get(itemId);
    }

    @Override
    public Collection<Item> getAllItems() {
        return itemTable.values();
    }

    private Long getLastId() {
        return ++lastId;
    }
}
