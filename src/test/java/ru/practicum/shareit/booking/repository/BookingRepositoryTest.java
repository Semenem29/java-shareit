package ru.practicum.shareit.booking.repository;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.store.BookingJPARepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemJPARepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserJPARepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BookingRepositoryTest {

    @Autowired
    BookingJPARepository bookingJPARepository;
    @Autowired
    UserJPARepository userJPARepository;
    @Autowired
    ItemJPARepository itemJPARepository;

    User owner;
    User booker;
    Item item;
    Booking waitingBooking;
    Booking approvedBooking;
    Booking rejectedBooking;
    Booking currentBooking;
    Booking pastBooking;
    Long ownerId;
    Long bookerId;
    Long itemId;
    Pageable page;

    @BeforeEach
    public void init() {
        page = PageRequest.of(0, 10);

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

        item = Item.builder()
                .id(1L)
                .name("spoon")
                .description("big and silver")
                .available(true)
                .owner(owner)
                .build();
        item = itemJPARepository.save(item);
        itemId = item.getId();

        waitingBooking = Booking.builder()
                .start(LocalDateTime.of(2030, 1, 1, 1, 1, 1))
                .end(LocalDateTime.of(2030, 2, 1, 1, 1, 1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        approvedBooking = waitingBooking.toBuilder()
                .start(LocalDateTime.of(2030, 1, 1, 4, 1, 1))
                .status(BookingStatus.APPROVED)
                .build();
        rejectedBooking = waitingBooking.toBuilder()
                .start(LocalDateTime.of(2030, 1, 1, 1, 1, 0))
                .status(BookingStatus.REJECTED)
                .build();
        currentBooking = approvedBooking.toBuilder()
                .start(LocalDateTime.of(2023, 1, 1, 1, 1, 1))
                .end(LocalDateTime.of(2035, 2, 1, 1, 1, 1))
                .build();
        pastBooking = approvedBooking.toBuilder()
                .start(LocalDateTime.of(2007, 1, 1, 1, 1, 1))
                .end(LocalDateTime.of(2008, 1, 1, 1, 1, 1))
                .build();

        bookingJPARepository.save(waitingBooking);
        bookingJPARepository.save(approvedBooking);
        bookingJPARepository.save(rejectedBooking);
        bookingJPARepository.save(pastBooking);
        bookingJPARepository.save(currentBooking);
    }

    @Test
    public void findAllByItemOwnerIdOrderByStartDesc() {
        List<Booking> result = bookingJPARepository
                .findAllByItemOwnerIdOrderByStartDesc(ownerId, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(5)
                .contains(waitingBooking)
                .contains(approvedBooking)
                .contains(rejectedBooking)
                .contains(currentBooking)
                .contains(pastBooking)
                .startsWith(approvedBooking)
                .endsWith(pastBooking);
    }

    @Test
    public void findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = bookingJPARepository
                .findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(ownerId, now, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .doesNotContain(currentBooking)
                .contains(pastBooking)
                .startsWith(pastBooking)
                .endsWith(pastBooking);
    }

    @Test
    public void findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = bookingJPARepository
                .findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(ownerId, now, now, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .doesNotContain(pastBooking)
                .contains(currentBooking)
                .startsWith(currentBooking)
                .endsWith(currentBooking);
    }

    @Test
    public void findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc() {

        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = bookingJPARepository
                .findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(ownerId, now, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(3)
                .contains(waitingBooking)
                .contains(approvedBooking)
                .contains(rejectedBooking)
                .startsWith(approvedBooking)
                .endsWith(rejectedBooking);
    }

    @Test
    public void findAllByItemOwnerIdAndStatusInOrderByStartDesc() {

        List<BookingStatus> notApprovedStatus = List.of(BookingStatus.REJECTED, BookingStatus.CANCELED);
        List<Booking> result = bookingJPARepository
                .findAllByItemOwnerIdAndStatusInOrderByStartDesc(ownerId, notApprovedStatus, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .contains(rejectedBooking)
                .startsWith(rejectedBooking)
                .endsWith(rejectedBooking);

    }

    @Test
    public void findAllByItemOwnerIdAndStatusOrderByStartDesc() {
        List<Booking> result = bookingJPARepository
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .contains(waitingBooking)
                .doesNotContain(approvedBooking)
                .doesNotContain(rejectedBooking)
                .startsWith(waitingBooking)
                .endsWith(waitingBooking);
    }

    @Test
    public void findAllByBookerIdOrderByStartDesc() {

        List<Booking> result = bookingJPARepository
                .findAllByBookerIdOrderByStartDesc(bookerId, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(5)
                .contains(waitingBooking)
                .contains(approvedBooking)
                .contains(currentBooking)
                .contains(rejectedBooking)
                .contains(pastBooking)
                .startsWith(approvedBooking)
                .endsWith(pastBooking);
    }

    @Test
    public void findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc() {

        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = bookingJPARepository
                .findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(bookerId, now, now, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .contains(currentBooking)
                .doesNotContain(approvedBooking)
                .doesNotContain(rejectedBooking)
                .startsWith(currentBooking)
                .endsWith(currentBooking);
    }

    @Test
    public void findAllByBookerIdAndEndIsBeforeOrderByStartDesc() {

        LocalDateTime now = LocalDateTime.now();

        List<Booking> result = bookingJPARepository
                .findAllByBookerIdAndEndIsBeforeOrderByStartDesc(bookerId, now, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .contains(pastBooking)
                .startsWith(pastBooking)
                .endsWith(pastBooking);
    }

    @Test
    public void findAllByBookerIdAndStartIsAfterOrderByStartDesc() {

        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = bookingJPARepository
                .findAllByBookerIdAndStartIsAfterOrderByStartDesc(bookerId, now, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(3)
                .contains(waitingBooking)
                .contains(approvedBooking)
                .contains(rejectedBooking)
                .startsWith(approvedBooking)
                .endsWith(rejectedBooking);
    }

    @Test
    public void findAllByBookerIdAndStatusInOrderByStartDesc() {

        List<BookingStatus> notApprovedStatus = List.of(BookingStatus.REJECTED, BookingStatus.CANCELED);
        List<Booking> result = bookingJPARepository
                .findAllByBookerIdAndStatusInOrderByStartDesc(bookerId, notApprovedStatus, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .contains(rejectedBooking)
                .startsWith(rejectedBooking)
                .endsWith(rejectedBooking);
    }

    @Test
    public void findAllByBookerIdAndStatusOrderByStartDesc() {

        List<Booking> result = bookingJPARepository
                .findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING, page);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(1)
                .contains(waitingBooking)
                .startsWith(waitingBooking)
                .endsWith(waitingBooking);
    }

    @Test
    public void findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc() {

        LocalDateTime now = LocalDateTime.now();

        Optional<Booking> lastBooking = bookingJPARepository
                .findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(itemId,
                        BookingStatus.APPROVED, now, now);

        AssertionsForClassTypes.assertThat(lastBooking).hasValueSatisfying(booking -> assertThat(booking)
                .hasFieldOrPropertyWithValue("id", 5L)
                .hasFieldOrPropertyWithValue("start", currentBooking.getStart())
                .hasFieldOrPropertyWithValue("end", currentBooking.getEnd())
                .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("booker", booker));
    }

    @Test
    public void findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart() {

        LocalDateTime now = LocalDateTime.now();

        Optional<Booking> lastBooking = bookingJPARepository
                .findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(itemId,
                        BookingStatus.APPROVED, now, now);

        AssertionsForClassTypes.assertThat(lastBooking).hasValueSatisfying(booking -> assertThat(booking)
                .hasFieldOrPropertyWithValue("id", 2L)
                .hasFieldOrPropertyWithValue("start", approvedBooking.getStart())
                .hasFieldOrPropertyWithValue("end", approvedBooking.getEnd())
                .hasFieldOrPropertyWithValue("status", BookingStatus.APPROVED)
                .hasFieldOrPropertyWithValue("item", item)
                .hasFieldOrPropertyWithValue("booker", booker));
    }

    @Test
    public void findAllByItemIdAndBookerIdAndStatusAndStartIsBefore() {

        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = bookingJPARepository
                .findAllByItemIdAndBookerIdAndStatusAndStartIsBefore(itemId, bookerId, BookingStatus.APPROVED, now);

        AssertionsForClassTypes.assertThat(result).asList()
                .hasSize(2)
                .startsWith(pastBooking)
                .endsWith(currentBooking);
    }

    @AfterEach
    public void destroy() {
        userJPARepository.deleteAll();
        itemJPARepository.deleteAll();
        bookingJPARepository.deleteAll();
    }

}
