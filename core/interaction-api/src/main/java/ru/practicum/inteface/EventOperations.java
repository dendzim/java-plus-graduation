package ru.practicum.inteface;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.EventFullDto;
import ru.practicum.enums.EventState;

public interface EventOperations {

    @GetMapping("/{eventId}")
    EventFullDto getEventById(@PathVariable Long eventId);

    @GetMapping("/{eventId}/check")
    boolean existsById(@PathVariable Long eventId);
}
