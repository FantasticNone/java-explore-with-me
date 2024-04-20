package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.event.Event;

import java.util.List;

public interface EventsRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByInitiatorId(long userId, Pageable pageable);

    @Query( "SELECT e " +
            "FROM Event e " +
            "WHERE e.category.id = :id")
    List<Event> findEventByCategory(long id);

    @Query( "SELECT e " +
            "FROM Event e " +
            "WHERE 1=1 " +
            "AND :ids IS NULL OR e.id IN :ids")
    List<Event> findAllByIdIn(List<Long> ids);

    @Query( "SELECT e " +
            "FROM Event e " +
            "WHERE e.id IN :ids")
    List<Event> findEventsByIds(List<Long> ids);

    List<Event> findAll(Specification<Event> specification, Pageable pageable);
}
