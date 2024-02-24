package ru.practicum.shareit.request.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ItemRequestJPARepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(Long requesterId);

    List<ItemRequest> findAllByRequesterIdIsNotOrderByCreatedDesc(Long requester, Pageable pageRequest);
}
