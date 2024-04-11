package ru.practicum.ewm.dto.compilation;


import lombok.*;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class NewCompilationDto {

    @Nullable
    private List<Long> events = Collections.emptyList();

    @Nullable
    private Boolean pinned = false;
    @NotBlank
    @Size(min = 1, max = 50, message = "Title size is out of bounds")
    private String title;
}