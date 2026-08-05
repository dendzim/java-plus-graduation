package ru.practicum.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventRequestCountDto;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.ParticipationRequestDto;
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
