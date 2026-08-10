package ru.practicum.dto.participation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventRequestCountDto {
    private Long eventId;
    private Long count;

    public EventRequestCountDto() {
    }

    public EventRequestCountDto(Long eventId, Long count) {
        this.eventId = eventId;
        this.count = count != null ? count : 0L;  // ← Long в Long
    }
}
