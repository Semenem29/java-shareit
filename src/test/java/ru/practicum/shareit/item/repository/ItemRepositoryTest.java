package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemJPARepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.store.ItemRequestJPARepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJPARepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ItemRepositoryTest {
    @Autowired
    UserJPARepository userRepository;
    @Autowired
    ItemJPARepository itemRepository;
    @Autowired
    ItemRequestJPARepository itemRequestRepository;
    User owner;
    User requester;
    Item item1;
    Item item2;
    ItemRequest itemRequest1;
    ItemRequest itemRequest2;
    Long ownerId;
    Long requesterId;
    Long itemRequestId1;
    Long itemRequestId2;
    Pageable page;

    @BeforeEach
    public void init() {
        owner = User.builder()
                .name("John")
                .email("tinkoff@yandex.ru")
                .build();
        owner = userRepository.save(owner);
        ownerId = owner.getId();

        requester = User.builder()
                .name("Peter")
                .email("alpha@yandex.ru")
                .build();
        requester = userRepository.save(requester);
        requesterId = requester.getId();

        itemRequest1 = ItemRequest.builder()
                .description("I need a spoon")
                .requester(requester)
                .created(LocalDateTime.of(2024, 1, 1, 1, 1, 1))
                .build();

        itemRequest2 = ItemRequest.builder()
                .description("I kooking for a shirt")
                .requester(requester)
                .created(LocalDateTime.now())
                .build();

        itemRequestRepository.save(itemRequest1);
        itemRequestId1 = itemRequest1.getId();
        itemRequestRepository.save(itemRequest2);
        itemRequestId2 = itemRequest2.getId();

        item1 = Item.builder()
                .name("spoon")
                .description("steal big spoon, never used")
                .available(true)
                .owner(owner)
                .request(itemRequest1)
                .build();
        item1 = itemRepository.save(item1);

        item2 = Item.builder()
                .name("shirt")
                .description("bluE shirt")
                .available(true)
                .owner(owner)
                .request(itemRequest2)
                .build();
        item2 = itemRepository.save(item2);

        page = PageRequest.of(0, 10);
    }

    @Test
    public void findAllByOwnerId() {

        List<Item> result = itemRepository.findAllByOwnerId(ownerId, page);

        assertThat(result).asList()
                .hasSize(2)
                .contains(item1)
                .contains(item2);
    }

    @Test
    public void searchItemBySubstring() {

        List<Item> resultTwoItems = itemRepository.searchItemBySubstring("e", page);

        assertThat(resultTwoItems).asList()
                .hasSize(2)
                .contains(item1)
                .contains(item2);

        List<Item> resultOneItem = itemRepository.searchItemBySubstring("spOOn", page);

        assertThat(resultOneItem).asList()
                .hasSize(1)
                .doesNotContain(item2)
                .contains(item1);

        List<Item> resultNoItem = itemRepository.searchItemBySubstring("invalid", page);
        assertThat(resultNoItem).asList()
                .isEmpty();
    }

    @Test
    public void findAllByRequestIn() {

        List<ItemRequest> requests = List.of(itemRequest1, itemRequest2);

        List<Item> result = itemRepository.findAllByRequestIn(requests);

        assertThat(result).asList()
                .hasSize(2)
                .contains(item1)
                .contains(item2);
    }

    @Test
    public void findAllByRequestId() {

        List<Item> result1 = itemRepository.findAllByRequestId(itemRequestId2);

        assertThat(result1).asList()
                .hasSize(1)
                .contains(item2)
                .doesNotContain(item1);

        List<Item> result2 = itemRepository.findAllByRequestId(itemRequestId1);

        assertThat(result2).asList()
                .hasSize(1)
                .contains(item1)
                .doesNotContain(item2);
    }

    @AfterEach
    public void destroy() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
    }
}
