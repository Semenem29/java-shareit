package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AttempToUpdateNotYourItemException;
import ru.practicum.shareit.exception.UserNotExistException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    @Autowired
    private final ItemRepository itemRepository;
    @Autowired
    private final UserRepository userRepository;

    @Override
    public Item createItem(ItemDto itemDto, Long ownerId) {
        if (itemDto == null || ownerId == null) {
            throw new ValidationException("provided data cannot be null");
        }

        validateOwnerId(ownerId);

        Item newItem = ItemMapper.toItem(itemDto);
        validateItem(newItem);
        newItem.setOwner(userRepository.getUserById(ownerId));

        return itemRepository.createItem(newItem);
    }

    @Override
    public Item updateItem(ItemDto itemDto, Long ownerId, Long itemId) {
        if (itemDto == null || ownerId == null || itemId == null) {
            throw new ValidationException("provided data cannot be null");
        }
        validateOwnerId(ownerId);

        Item updatedItem = ItemMapper.toItem(itemDto);
        Item storedItem = itemRepository.getItemById(itemId);

        if (!storedItem.getOwner().getId().equals(ownerId)) {
            throw new AttempToUpdateNotYourItemException("attemp to update someone else's item!!!");
        }

        if (updatedItem.getRequest() != null) {
            throw new ValidationException("you can't change the request field!");
        }

        if (updatedItem.getName() == null) {
            updatedItem.setName(storedItem.getName());
        }

        if (updatedItem.getDescription() == null) {
            updatedItem.setDescription(storedItem.getDescription());
        }

        if (updatedItem.getAvailable() == null) {
            updatedItem.setAvailable(storedItem.getAvailable());
        }

        updatedItem.setOwner(storedItem.getOwner());

        return itemRepository.updateItem(updatedItem, itemId);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        if (itemId == null) {
            throw new ValidationException("provided data cannot be null");
        }

        return ItemMapper.toItemDto(itemRepository.getItemById(itemId));
    }

    @Override
    public Collection<ItemDto> getItemsOfOwner(Long ownerId) {
        if (ownerId == null) {
            throw new ValidationException("provided data cannot be null");
        }

        return itemRepository.getAllItems().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> findItemsByText(String text) {
        if (text == null) {
            throw new ValidationException("provided data cannot be null");
        }

        if (text.isBlank()) {
            return new ArrayList<>();
        }

        return itemRepository.getAllItems().stream()
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void validateItem(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("invalid or empty item name!");
        }

        if (item.getAvailable() == null) {
            throw new ValidationException("available status MUST be provided!");
        }

        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("invalid or empty item description!");
        }
    }

    private void validateOwnerId(Long ownerId) {

        if (userRepository.getUserById(ownerId) == null) {
            throw new UserNotExistException("attemp to create item with not existed owner");
        }
    }
}
