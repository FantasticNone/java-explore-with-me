package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.EventLocation;

public interface EventLocationRepository extends JpaRepository<EventLocation, Long> {
    Boolean existsByLatAndLon(float lat, float lon);

    EventLocation findByLatAndLon(Float lat, Float lon);
}
