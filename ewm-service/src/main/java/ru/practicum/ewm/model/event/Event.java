package ru.practicum.ewm.model.event;

import javax.persistence.*;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.EventLocation;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 2000)
    private String annotation;
    @Column(length = 120)
    private String title;
    private int confirmedRequests;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;
    private LocalDateTime eventDate;
    @Column(length = 7000)
    private String description;
    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @OneToMany(mappedBy = "event")
    private List<Request> requests;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private EventLocation eventLocation;

    @Embedded
    private Location location;

    private Boolean paid;
    private Boolean requestModeration;
    private Long participantLimit;
    @Enumerated(value = EnumType.STRING)
    private EventState state;
    //private Integer views;
}