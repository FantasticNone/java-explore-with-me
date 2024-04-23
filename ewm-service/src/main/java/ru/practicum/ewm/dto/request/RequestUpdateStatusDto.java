package ru.practicum.ewm.dto.request;

import lombok.*;
import ru.practicum.ewm.model.request.RequestStatus;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class RequestUpdateStatusDto {

    private List<Long> requestIds;
    private RequestStatus status;
}
