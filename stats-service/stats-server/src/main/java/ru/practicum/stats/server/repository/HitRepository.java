package ru.practicum.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HitRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT e FROM EndpointHit AS e " +
            "WHERE e.timestamp >= :start AND e.timestamp <= :end")
    List<EndpointHit> findAllHitsBetweenDates(LocalDateTime start, LocalDateTime end);

    @Query("SELECT e FROM EndpointHit AS e " +
            "WHERE e.timestamp >= :start AND e.timestamp <= :end " +
            "AND e.uri IN (:uris)")
    List<EndpointHit> findByTimestampAndUris(LocalDateTime start, LocalDateTime end, List<String> uris);
}
