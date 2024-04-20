package ru.practicum.ewm.model.event;

import javax.persistence.*;
import javax.validation.constraints.Size;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.request.Request;
import ru.practicum.ewm.model.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 20, max = 2000)
    @Column(nullable = false)
    private String annotation;

    @Size(min = 3, max = 120)
    @Column(name = "title")
    private String title;

    @Column(name = "confirmed_request")
    private int confirmedRequests = 0;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;
    private LocalDateTime eventDate;

    @Size(min = 20, max = 7000)
    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @OneToMany(mappedBy = "event")
    private List<Request> requests;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    private Boolean paid= false;
    private Boolean requestModeration;
    private Integer participantLimit;

    @Enumerated(value = EnumType.STRING)
    private EventState state;

    private long views = 0;
}