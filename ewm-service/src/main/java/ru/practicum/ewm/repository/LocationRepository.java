package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.event.Location;


public interface LocationRepository extends JpaRepository<Location, Long> {

    Boolean existsByLatAndLon(float lat, float lon);

}
