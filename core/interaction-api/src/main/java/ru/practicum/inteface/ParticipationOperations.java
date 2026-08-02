package ru.practicum.inteface;

import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventRequestCountDto;
import ru.practicum.enums.ParticipationStatus;

import java.util.List;

public interface ParticipationOperations {

    @GetMapping("/count/{eventId}")
    int countByEventIdAndStatus(@PathVariable Long eventId, @RequestParam ParticipationStatus status);

    @PostMapping("/count/confirmed")
    List<EventRequestCountDto> countConfirmedRequestsByEventIds(@RequestBody List<Long> eventIds,
                                                                @RequestParam ParticipationStatus status);

}
