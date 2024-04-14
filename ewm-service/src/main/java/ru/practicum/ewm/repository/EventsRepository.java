package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;

import java.time.LocalDateTime;
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

    /*@Query("Select e from Event e " +
            "Where e.id IN :ids")
    List<Event> findEventsByIds(List<Long> ids);*/

    @Query("SELECT e FROM Event AS e " +
            "WHERE e.initiator.id IN ?1 " +
            "AND e.category.id IN ?2 " +
            "AND e.state IN ?3 " +
            "AND e.eventDate BETWEEN ?4 AND ?5")
    List<Event> findByUsersAndCategoriesWithTimestamp(List<Long> users, List<Long> categories, List<EventState> states, LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event AS e " +
            "WHERE e.initiator.id IN ?1 " +
            "AND e.category.id IN ?2 " +
            "AND e.state IN ?3")
    List<Event> findByUsersAndCategories(List<Long> users, List<Long> categories, List<EventState> states, Pageable pageable);

    @Query("SELECT e FROM Event AS e " +
            "WHERE e.initiator.id IN ?1 " +
            "AND e.state IN ?2 " +
            "AND e.eventDate BETWEEN ?3 AND ?4")
    List<Event> findByUsersWithTimestamp(List<Long> users, List<EventState> states, LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event AS e " +
            "WHERE e.initiator.id IN ?1 " +
            "AND e.state IN ?2")
    List<Event> findByUsers(List<Long> users, List<EventState> states, Pageable pageable);

    @Query("SELECT e FROM Event AS e " +
            "WHERE e.category.id IN ?1 " +
            "AND e.state IN ?2 " +
            "AND e.eventDate BETWEEN ?3 AND ?4")
    List<Event> findByCategoriesWithTimestamp(List<Long> categories, List<EventState> states, LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event AS e " +
            "WHERE e.category.id IN ?1 " +
            "AND e.state IN ?2")
    List<Event> findByCategories(List<Long> categories, List<EventState> states, Pageable pageable);

    @Query("SELECT e FROM Event AS e " +
            "WHERE e.state IN ?1 " +
            "AND e.eventDate BETWEEN ?2 AND ?3")
    List<Event> findWithTimestamp(List<EventState> states, LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event AS e " +
            "WHERE e.state IN ?1")
    List<Event> findByStates(List<EventState> states, Pageable pageable);

    List<Event> findAll(Specification<Event> specification, Pageable pageable);

}
