package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.participation.EventRequestCountDto;
import ru.practicum.enums.ParticipationStatus;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

	List<ParticipationRequest> findByRequesterId(Long requesterId);

	List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);

	List<ParticipationRequest> findByEventId(Long eventId);

	int countByEventIdAndStatus(Long eventId, ParticipationStatus status);

	@Query("""
			SELECT new ru.practicum.dto.EventRequestCountDto(r.eventId, COUNT(r) as count)
			FROM ParticipationRequest r
			WHERE r.eventId IN :eventIds
			AND r.status = :status
			GROUP BY r.eventId
			""")
	List<EventRequestCountDto> countConfirmedRequestsByEventIds(List<Long> eventIds, ParticipationStatus status);

	@Modifying
	@Transactional
	@Query("""
			UPDATE ParticipationRequest pr
			SET pr.status = 'REJECTED'
			WHERE pr.eventId = :eventId
			AND pr.status=:status
			""")
	int rejectPendingRequests(Long eventId, ParticipationStatus status);

	boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);
}
