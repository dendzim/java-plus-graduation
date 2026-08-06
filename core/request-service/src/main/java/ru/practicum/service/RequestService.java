package ru.practicum.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.participation.EventRequestCountDto;
import ru.practicum.dto.participation.EventRequestStatusUpdateRequest;
import ru.practicum.dto.participation.EventRequestStatusUpdateResult;
import ru.practicum.dto.participation.ParticipationRequestDto;
import ru.practicum.enums.ParticipationStatus;

import java.util.List;

@Transactional
public interface RequestService {

	List<ParticipationRequestDto> findByEventId(Long userId, Long eventId);

	EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
	                                                   EventRequestStatusUpdateRequest request);

	List<ParticipationRequestDto> findByRequesterId(Long userId);

	ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

	ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId);

	int countByEventIdAndStatus(Long eventId, ParticipationStatus participationStatus);

	List<EventRequestCountDto> countConfirmedRequestsByEventIds(List<Long> eventIds, ParticipationStatus status);
}
