package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentJPARepository;
import ru.practicum.shareit.item.storage.ItemJPARepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJPARepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CommentRepositoryTest {
    @Autowired
    UserJPARepository userJPARepository;
    @Autowired
    ItemJPARepository itemJPARepository;
    @Autowired
    CommentJPARepository commentJPARepository;

    User owner;
    User booker;
    Item item1;
    Item item2;
    ItemRequest itemRequest1;
    ItemRequest itemRequest2;
    Comment comment1toItem1;
    Comment comment2toItem1;
    Comment comment1toItem2;
    Long ownerId;
    Long bookerId;
    Long item1Id;

    @BeforeEach
    public void init() {
        owner = User.builder()
                .name("Oleg")
                .email("tinkoff@gmail.com")
                .build();
        owner = userJPARepository.save(owner);
        ownerId = owner.getId();

        booker = User.builder()
                .name("Peter")
                .email("griffin@gmail.com")
                .build();
        booker = userJPARepository.save(booker);
        bookerId = booker.getId();

        item1 = Item.builder()
                .name("spoon")
                .description("small and iron")
                .available(true)
                .owner(owner)
                .request(itemRequest1)
                .build();
        item1 = itemJPARepository.save(item1);
        item1Id = item1.getId();

        item2 = Item.builder()
                .name("knife")
                .description("blaCk")
                .available(true)
                .owner(owner)
                .request(itemRequest2)
                .build();
        item2 = itemJPARepository.save(item2);

        comment1toItem1 = Comment.builder()
                .text("comment to Item 1")
                .author(booker)
                .item(item1)
                .created(LocalDateTime.now())
                .build();
        commentJPARepository.save(comment1toItem1);

        comment2toItem1 = Comment.builder()
                .text("another comment to Item 1")
                .author(booker)
                .item(item1)
                .created(LocalDateTime.now().minusMonths(1))
                .build();
        commentJPARepository.save(comment2toItem1);

        comment1toItem2 = Comment.builder()
                .text("comment to Item 2")
                .author(booker)
                .item(item2)
                .created(LocalDateTime.now().minusMonths(1))
                .build();
        commentJPARepository.save(comment1toItem2);
    }

    @AfterEach
    public void destroy() {
        userJPARepository.deleteAll();
        itemJPARepository.deleteAll();
        commentJPARepository.deleteAll();
    }

    @Test
    public void findAllByItemId() {

        List<Comment> result = commentJPARepository.findAllByItemId(item1Id);

        assertThat(result).asList()
                .hasSize(2)
                .contains(comment1toItem1)
                .contains(comment2toItem1)
                .doesNotContain(comment1toItem2);
    }

    @Test
    public void findAllByItemIn() {

        List<Item> items = List.of(item1, item2);
        List<Comment> result = commentJPARepository.findAllByItemIn(items);

        assertThat(result).asList()
                .hasSize(3)
                .contains(comment1toItem1)
                .contains(comment2toItem1)
                .contains(comment1toItem2);
    }

}
