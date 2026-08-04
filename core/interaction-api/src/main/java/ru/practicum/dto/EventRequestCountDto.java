package ru.practicum.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventRequestCountDto {
    private Long eventId;
    private Integer count;

    public EventRequestCountDto() {
    }

    public EventRequestCountDto(Long eventId, Integer count) {
        this.eventId = eventId;
        this.count = count;
    }

    public EventRequestCountDto(Long eventId, Long count) {
        this.eventId = eventId;
        this.count = count != null ? count.intValue() : 0;
    }
}
