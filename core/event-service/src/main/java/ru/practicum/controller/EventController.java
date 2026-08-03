package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EventFullDto;
import ru.practicum.enums.EventState;
import ru.practicum.feignClient.EventClient;
import ru.practicum.service.EventService;

@RestController
@RequestMapping(path = "/api/event")
@RequiredArgsConstructor
public class EventController implements EventClient {

    private final EventService eventService;

    @Override
    public EventFullDto getEventById(Long eventId) {
        return eventService.getEventById(eventId);
    }

    @Override
    public EventFullDto findByIdAndState(Long id, EventState state) {
        return eventService.;
    }

    @Override
    public EventFullDto updateEventRate(Long id) {
        return eventService.updateEventRate(id);
    }
}
