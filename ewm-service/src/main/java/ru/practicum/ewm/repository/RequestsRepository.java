package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.model.User;

import java.util.List;

public interface RequestsRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEventIdAndStatus(long eventId, Request.Status status);

    @Query("SELECT r FROM Request AS r " +
            "WHERE r.requester = ?1")
    List<Request> findAllByUser(User user);
}
