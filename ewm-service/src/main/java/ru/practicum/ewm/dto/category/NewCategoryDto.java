package ru.practicum.ewm.dto.category;

import lombok.*;

import javax.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCategoryDto {
    @NotBlank
    @Size(min = 1, max = 50, message = "Name size is out of bounds")
    private String name;
}
