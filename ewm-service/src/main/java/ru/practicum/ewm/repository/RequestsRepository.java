package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.request.Request;
import ru.practicum.ewm.model.user.User;

import java.util.List;

public interface RequestsRepository extends JpaRepository<Request, Long> {

    @Query( "SELECT r "+
            "FROM Request r " +
            "WHERE r.id IN :ids")
    List<Request> findRequestsByIds(List<Long> ids);

    List<Request> findAllByRequester(User requester);
}
