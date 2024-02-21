package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingItemResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.store.BookingJPARepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentJPARepository;
import ru.practicum.shareit.item.storage.ItemJPARepository;
import ru.practicum.shareit.user.storage.UserJPARepository;
import ru.practicum.shareit.user.model.User;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemJPAServiceImpl implements ItemJPAService {

    private final ItemJPARepository itemJPARepository;
    private final UserJPARepository userJPARepository;
    private final BookingJPARepository bookingJPARepository;
    private final CommentJPARepository commentJPARepository;

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        User owner = getUserOrThrow(ownerId);
        Item item = ItemMapper.toItem(itemDto, owner, null);

        itemJPARepository.save(item);
        log.info("Saved the item: {}", item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public ItemDto updateItem(ItemDto itemDto, Long ownerId, Long itemId) {
        User owner = getUserOrThrow(ownerId);
        Item item = getItemOrThrow(itemId);

        Item updatedItem = updateValidFields(item, itemDto);
        if (!(isOwner(updatedItem, ownerId))) {
            String message = "you are not allow to edit the item, because you are not the owner";
            log.error("AttempToUpdateNotYourItemException: " + message);
            throw new AttempToUpdateNotYourItemException(message);
        }

        itemJPARepository.save(updatedItem);
        log.info("Updated the item: {}", updatedItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponseDto getItemById(Long userId, Long itemId) {
        Item item = getItemOrThrow(itemId);
        LocalDateTime now = LocalDateTime.now();
        BookingItemResponseDto lastBooking = null;
        BookingItemResponseDto nextBooking = null;
        if (Objects.equals(item.getOwner().getId(), userId)) {
            lastBooking = getLastBooking(itemId, now);
            nextBooking = getNextBooking(itemId, now);
        }

        List<CommentResponseDto> commentsDto = getCommentsByItemId(itemId);
        ItemResponseDto itemResponseDto = ItemMapper.toItemResponseDto(item, lastBooking, nextBooking, commentsDto);
        log.info("Provided info to user with id={}, about the item={}", userId, itemResponseDto);
        return itemResponseDto;
    }

    @Override
    @Transactional
    public List<ItemResponseDto> getItemsOfOwner(Long ownerId) {
        List<Item> items = itemJPARepository.findAllByOwnerId(ownerId);
        Map<Item, List<Comment>> commentsTable = getCommentOfItems(items);
        List<ItemResponseDto> itemResponseDtos = items.stream()
                .map(item -> toItemResponseDto(
                        item,
                        commentsTable.getOrDefault(item, Collections.emptyList()),
                        LocalDateTime.now()
                ))
                .collect(Collectors.toList());

        log.info("Provided info to owner with id={}, about his/her items, number of items={}",
                ownerId, itemResponseDtos.size());
        return itemResponseDtos;
    }

    @Override
    @Transactional
    public List<ItemResponseDto> findItemsByText(String text) {
        if (text.isBlank()) {
            log.info("Provided empty list");
            return new ArrayList<>();
        }

        List<Item> items = itemJPARepository.searchItemBySubstring(text);
        Map<Item, List<Comment>> commentsTable = getCommentOfItems(items);
        List<ItemResponseDto> itemResponseDtos = items.stream()
                .map(item -> toItemResponseDto(
                        item,
                        commentsTable.getOrDefault(item, Collections.emptyList()),
                        LocalDateTime.now()
                ))
                .collect(Collectors.toList());

        log.info("Provided info with list of items, number of items={}", itemResponseDtos.size());
        return itemResponseDtos;
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        if (isOwner(item, userId)) {
            String message = "it's forbidden to comment your own item";
            log.error("ValidationException: " + message);
            throw new ValidationException(message);
        }

        validAccessToAddComment(userId, itemId);
        Comment comment = CommentMapper.toComment(commentRequestDto, author, item);
        commentJPARepository.save(comment);
        log.info("Created the comment with authorId={}, itemId={}, and comment={}", userId, itemId, comment);
        return CommentMapper.toCommentResponseDto(comment);
    }

    private void validAccessToAddComment(Long userId, Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        BookingStatus bookingStatus = BookingStatus.APPROVED;
        List<Booking> bookings = bookingJPARepository
                .findAllByItemIdAndBookerIdAndStatusAndStartIsBefore(itemId, userId, bookingStatus, now);

        if (bookings.isEmpty()) {
            String message = "it's forbidden to comment item you've never booked";
            log.error("ValidationException: " + message);
            throw new ValidationException("it's forbidden to comment item you've never booked");
        }
    }

    private ItemResponseDto toItemResponseDto(Item item, List<Comment> comments, LocalDateTime now) {
        BookingItemResponseDto lastBooking = getLastBooking(item.getId(), now);
        BookingItemResponseDto nextBooking = getNextBooking(item.getId(), now);
        List<CommentResponseDto> commentsDto = CommentMapper.toCommentResponseDtoList(comments);

        return ItemMapper.toItemResponseDto(item, lastBooking, nextBooking, commentsDto);
    }

    private Map<Item, List<Comment>> getCommentOfItems(List<Item> items) {
        List<Comment> comments = commentJPARepository.findAllByItemIn(items);

        return comments.stream()
                .collect(Collectors.groupingBy(Comment::getItem));
    }

    private User getUserOrThrow(Long userId) {
        Optional<User> userOpt = userJPARepository.findById(userId);
        if (userOpt.isEmpty()) {
            String message = "there is no user with id: " + userId;
            log.error("UserNotExistException: " + message);
            throw new UserNotExistException(message);
        }

        return userOpt.get();
    }

    private BookingItemResponseDto getLastBooking(Long itemId, LocalDateTime now) {
        return bookingJPARepository
                .findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(itemId,
                        BookingStatus.APPROVED,
                        now,
                        now)
                .map(BookingMapper::toBookingItemResponseDto)
                .orElse(null);
    }

    private BookingItemResponseDto getNextBooking(Long itemId, LocalDateTime now) {
        return bookingJPARepository
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(itemId,
                        BookingStatus.APPROVED,
                        now,
                        now)
                .map(BookingMapper::toBookingItemResponseDto)
                .orElse(null);
    }

    private List<CommentResponseDto> getCommentsByItemId(Long itemId) {
        List<Comment> comments = commentJPARepository.findAllByItemId(itemId);

        return CommentMapper.toCommentResponseDtoList(comments);
    }

    private Item updateValidFields(Item item, ItemDto newItem) {
        String newName = newItem.getName();
        String newDescription = newItem.getDescription();
        Boolean newIsAvailbale = newItem.getAvailable();

        if (newName != null && !newName.isBlank()) {
            item = item.toBuilder().name(newName).build();
        }

        if (newDescription != null && !newDescription.isBlank()) {
            item = item.toBuilder().description(newDescription).build();
        }

        if (newIsAvailbale != null) {
            item = item.toBuilder().available(newIsAvailbale).build();
        }

        return item;
    }

    private boolean isOwner(Item item, Long ownerId) {
        return Objects.equals(item.getOwner().getId(), ownerId);
    }

    private Item getItemOrThrow(Long itemId) {
        Optional<Item> itemOpt = itemJPARepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            String message = "there is no item with id: " + itemId;
            log.error("ItemNotExistException: " + message);
            throw new ItemNotExistException("there is no item with id: " + itemId);
        }

        return itemOpt.get();
    }
}
