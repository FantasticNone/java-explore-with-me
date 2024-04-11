package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.event.Event;

import java.util.List;

public interface EventsRepository extends JpaRepository<Event, Long> {
    @Query("Select e from Event e " +
            "Where e.initiator.id = :userId")
    List<Event> getUserEvents(long userId, Pageable page);

    @Query("Select count(e) from Event e where e.initiator.id = :userId")
    long countByInitiatorId(long userId);

    @Query("Select e from Event e " +
            "Where e.category.id = :id")
    List<Event> findEventByCategory(long id);

    @Query("Select e from Event e " +
            "Where e.id IN :ids")
    List<Event> findEventsByIds(List<Long> ids);
}
