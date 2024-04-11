package ru.practicum.ewm.model.event;

import lombok.*;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private Float lat;
    private Float lon;
}
