package ru.practicum.inteface;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.EventFullDto;
import ru.practicum.enums.EventState;

public interface EventOperations {

    @GetMapping("/{eventId}")
    EventFullDto getEventById(@PathVariable Long eventId);

    @GetMapping
    EventFullDto findByIdAndState(@RequestParam("id") Long id,
                                            @RequestParam("state") EventState state);

    @PatchMapping("/{id}")
    EventFullDto updateEventRate(@PathVariable("id") Long id);
}
