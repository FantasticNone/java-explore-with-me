package ru.practicum.ewm.dto.request;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class RequestListDto {

    private List<RequestDto> confirmedRequests;
    private List<RequestDto> rejectedRequests;
}