package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.participation.EventRequestCountDto;
import ru.practicum.enums.ParticipationStatus;
import ru.practicum.feignClient.ParticipationClient;
import ru.practicum.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/api/request")
@RequiredArgsConstructor
public class ParticipationController implements ParticipationClient {

    private final RequestService requestService;

    @Override
    public int countByEventIdAndStatus(Long eventId, ParticipationStatus status) {
        return requestService.countByEventIdAndStatus(eventId, status);
    }

    @Override
    public List<EventRequestCountDto> countConfirmedRequestsByEventIds(List<Long> eventIds,
                                                                       ParticipationStatus status) {
        return requestService.countConfirmedRequestsByEventIds(eventIds, status);
    }
}
