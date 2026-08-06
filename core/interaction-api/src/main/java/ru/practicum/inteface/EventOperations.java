package ru.practicum.inteface;

import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.enums.EventState;

public interface EventOperations {

    @GetMapping("/{eventId}")
    EventFullDto getEventById(@PathVariable Long eventId);

    @GetMapping
    EventFullDto findByIdAndState(@RequestParam("id") Long id,
                                            @RequestParam("state") EventState state);

    @PutMapping
    EventFullDto updateEventRate(@RequestBody EventFullDto eventFullDto);
}
