package ru.practicum.ewm.dto.compilation;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {

    private List<Long> events;
    private boolean pinned = false;

    @NotBlank
    @Size(min = 1, max = 50, message = "Title size is out of bounds")
    private String title;
}