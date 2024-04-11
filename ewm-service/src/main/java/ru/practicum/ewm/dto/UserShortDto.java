package ru.practicum.ewm.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UserShortDto {
    private Long id;
    private String name;
}
