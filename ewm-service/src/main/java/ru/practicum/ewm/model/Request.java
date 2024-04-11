package ru.practicum.ewm.model;

import lombok.*;
import ru.practicum.ewm.model.event.Event;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @OneToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    @Enumerated(value = EnumType.STRING)
    private Status status;

    public enum Status {
        PENDING,
        CONFIRMED,
        REJECTED,
        CANCELED
    }
}