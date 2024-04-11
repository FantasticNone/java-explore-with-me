package ru.practicum.ewm.dto.category;

import lombok.*;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class CategoryDto {

    private long id;
    private String name;
}