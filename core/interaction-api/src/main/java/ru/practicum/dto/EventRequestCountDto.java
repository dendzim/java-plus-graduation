package ru.practicum.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventRequestCountDto {
    private Long eventId;
    private Long count;
}
