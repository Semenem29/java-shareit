package ru.practicum.shareit.booking.store;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingJPARepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageRequest);

    List<Booking> findAllByItemOwnerIdAndEndIsBeforeOrderByStartDesc(Long ownerId,
                                                                     LocalDateTime now,
                                                                     Pageable pageRequest);

    List<Booking> findAllByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long ownerId,
                                                                                    LocalDateTime now,
                                                                                    LocalDateTime anotherNow,
                                                                                    Pageable pageRequest);

    List<Booking> findAllByItemOwnerIdAndStartIsAfterOrderByStartDesc(Long ownerId,
                                                                      LocalDateTime now,
                                                                      Pageable pageRequest);

    List<Booking> findAllByItemOwnerIdAndStatusInOrderByStartDesc(Long ownerId,
                                                                  List<BookingStatus> rejectedStatuses,
                                                                  Pageable pageRequest);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId,
                                                                BookingStatus bookingStatus,
                                                                Pageable pageRequest);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId, Pageable pageRequest);

    List<Booking> findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long bookerId,
                                                                                 LocalDateTime now,
                                                                                 LocalDateTime anotherNow,
                                                                                 Pageable pageRequest);

    List<Booking> findAllByBookerIdAndEndIsBeforeOrderByStartDesc(Long bookerId,
                                                                  LocalDateTime now,
                                                                  Pageable pageRequest);

    List<Booking> findAllByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId,
                                                                   LocalDateTime now,
                                                                   Pageable pageRequest);

    List<Booking> findAllByBookerIdAndStatusInOrderByStartDesc(Long bookerId,
                                                               List<BookingStatus> rejectedStatuses,
                                                               Pageable pageRequest);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId,
                                                             BookingStatus bookingStatus,
                                                             Pageable pageRequest);

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
