package ru.practicum.ewm.model.event;

import lombok.*;

import javax.persistence.*;

@Embeddable
@Entity
@Table(name = "locations")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Float lat;

    @Column(nullable = false)
    private Float lon;
}