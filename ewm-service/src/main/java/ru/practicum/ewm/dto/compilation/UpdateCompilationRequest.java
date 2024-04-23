package ru.practicum.ewm.dto.compilation;

import lombok.*;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompilationRequest {

    private List<Long> events;
    private Boolean pinned;

    @Size(min = 1, max = 50, message = "Title size is out of bounds")
    private String title;
}
