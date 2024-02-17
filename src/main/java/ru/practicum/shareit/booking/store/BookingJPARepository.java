package ru.practicum.shareit.booking.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingJPARepository extends JpaRepository<Booking, Long> {
    Collection<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

    Collection<Booking> findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(Long ownerId,
                                                                           LocalDateTime now);

    Collection<Booking> findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long ownerId,
                                                                                          LocalDateTime now,
                                                                                          LocalDateTime anotherNow);

    Collection<Booking> findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(Long ownerId,
                                                                            LocalDateTime now);

    Collection<Booking> findAllByItemOwnerIdAndStatusInOrderByStartDesc(Long ownerId,
                                                                        List<BookingStatus> rejectedStatuses);

    Collection<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId,
                                                                      BookingStatus bookingStatus);

    Collection<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    Collection<Booking> findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long bookerId,
                                                                                       LocalDateTime now,
                                                                                       LocalDateTime anotherNow);

    Collection<Booking> findAllByBookerIdAndEndIsBeforeOrderByStartDesc(Long bookerId,
                                                                        LocalDateTime now);

    Collection<Booking> findAllByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId,
                                                                         LocalDateTime now);

    Collection<Booking> findAllByBookerIdAndStatusInOrderByStartDesc(Long bookerId,
                                                                     List<BookingStatus> rejectedStatuses);

    Collection<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus bookingStatus);

    Optional<Booking> findFirstByItemIdAndStatusAndStartIsBeforeOrStartEqualsOrderByEndDesc(Long itemId,
                                                                                            BookingStatus bookingStatus,
                                                                                            LocalDateTime now,
                                                                                            LocalDateTime anotherNow);

    Optional<Booking> findFirstByItemIdAndStatusAndStartIsAfterOrStartEqualsOrderByStart(Long itemId,
                                                                                         BookingStatus status,
                                                                                         LocalDateTime now,
                                                                                         LocalDateTime anotherNow);

    List<Booking> findAllByItemIdAndBookerIdAndStatusAndStartIsBefore(Long itemId,
                                                                      Long userId,
                                                                      BookingStatus bookingStatus,
                                                                      LocalDateTime now);
}
