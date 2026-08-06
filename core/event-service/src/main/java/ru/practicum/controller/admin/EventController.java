package ru.practicum.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.enums.EventState;
import ru.practicum.feignClient.EventClient;
import ru.practicum.service.event.EventService;

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
        return eventService.findByIdAndState(id, state);
    }

    @Override
    public EventFullDto updateEventRate(EventFullDto eventFullDto) {
        return eventService.updateEventRate(eventFullDto);
    }
}
