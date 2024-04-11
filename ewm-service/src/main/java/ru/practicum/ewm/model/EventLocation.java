package ru.practicum.ewm.model;

import lombok.*;
import ru.practicum.ewm.model.event.Event;

import javax.persistence.*;

@Entity
@Table(name = "event_locations")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EventLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Float lat;

    @Column(nullable = false)
    private Float lon;

    @OneToOne
    @MapsId
    private Event event;
}