package ru.practicum.service;

import com.google.protobuf.Timestamp;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.participation.EventRequestCountDto;
import ru.practicum.dto.participation.EventRequestStatusUpdateRequest;
import ru.practicum.dto.participation.EventRequestStatusUpdateResult;
import ru.practicum.dto.participation.ParticipationRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.action.UserActionProto;
import ru.practicum.repository.RequestRepository;
import ru.practicum.feignClient.EventClient;
import ru.practicum.feignClient.UserClient;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.enums.EventState;
import ru.practicum.enums.ParticipationStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.stat.client.CollectorClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestServiceImpl implements RequestService {

	private final RequestRepository requestRepository;
	private final EventClient eventClient;
	private final UserClient userClient;
	private final CollectorClient collectorClient;

	public List<ParticipationRequestDto> findByEventId(Long userId, Long eventId) {
		EventFullDto event = eventClient.getEventById(eventId);
		if (!event.getInitiator().id().equals(userId)) {
			throw new NotFoundException("Событие с id " + eventId + " не найдено");
		}
		return requestRepository.findByEventId(eventId)
				.stream()
				.map(RequestMapper::toParticipationRequestDto)
				.toList();
	}

	public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
															  EventRequestStatusUpdateRequest request) {
		EventFullDto event = getEventById(eventId);
		if (!event.getInitiator().id().equals(userId)) {
			throw new NotFoundException("Событие не найдено");
		}

		int limit = event.getParticipantLimit();
		List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
		List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

		boolean isModerationOff = !event.isRequestModeration() || limit == 0;
		boolean idsEmpty = request.requestIds() == null || request.requestIds().isEmpty();

		if (isModerationOff || idsEmpty) {
			return EventRequestStatusUpdateResult.builder()
					.confirmedRequests(Collections.emptyList())
					.rejectedRequests(Collections.emptyList())
					.build();
		}

		int countConfirmed = requestRepository.countByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED);
		List<ParticipationRequest> requests = requestRepository.findAllByIdIn(request.requestIds());

		if (request.status().name().equals(ParticipationStatus.CONFIRMED.name()) && countConfirmed >= limit) {
			throw new ConflictException("Достигнут лимит подтвержденных заявок");
		}

		for (ParticipationRequest pr : requests) {
			if (!pr.getStatus().equals(ParticipationStatus.PENDING)) {
				throw new ConflictException("Статус можно изменить только у заявок в состоянии рассмотрения");
			}

			if (request.status().name().equals(ParticipationStatus.CONFIRMED.name()) && countConfirmed < limit) {
				pr.setStatus(ParticipationStatus.CONFIRMED);
				countConfirmed++;
				confirmedRequests.add(RequestMapper.toParticipationRequestDto(pr));
			} else {
				pr.setStatus(ParticipationStatus.REJECTED);
				rejectedRequests.add(RequestMapper.toParticipationRequestDto(pr));
			}
		}

		requestRepository.saveAll(requests);

		// если в процессе лимит превышен - отклоняем все оставшиеся заявки
		if (request.status().name().equals(ParticipationStatus.CONFIRMED.name()) && countConfirmed >= limit) {
			if (requestRepository.rejectPendingRequests(eventId, ParticipationStatus.PENDING) < 0) {
				throw new RuntimeException("Не удалось отклонить заявку");
			}
		}

		return EventRequestStatusUpdateResult.builder()
				.confirmedRequests(confirmedRequests)
				.rejectedRequests(rejectedRequests)
				.build();
	}

	public List<ParticipationRequestDto> findByRequesterId(Long userId) {
		return requestRepository.findByRequesterId(userId)
				.stream()
				.map(RequestMapper::toParticipationRequestDto)
				.toList();
	}

	@Transactional
	public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
		UserDto requester = getUserById(userId);
		EventFullDto event = getEventById(eventId);


		if (!EventState.PUBLISHED.equals(event.getState())) {
			throw new ConflictException("Нельзя участвовать в неопубликованном событии");
		}

		if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
			throw new ConflictException("Запрос уже существует");
		}

		if (event.getInitiator().id().equals(userId)) {
			throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
		}

		int limit = event.getParticipantLimit();
		if (limit != 0) {
			long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED);

			if (event.isRequestModeration()) {
				long pendingCount = requestRepository.countByEventIdAndStatus(eventId, ParticipationStatus.PENDING);
				if (confirmedCount + pendingCount >= limit) {
					throw new ConflictException("Достигнут лимит запросов на участие");
				}
			} else {
				if (confirmedCount >= limit) {
					throw new ConflictException("Достигнут лимит запросов на участие");
				}
			}
		}

		ParticipationStatus status;

		if (!event.isRequestModeration() || limit == 0) {
			status = ParticipationStatus.CONFIRMED;
		} else {
			status = ParticipationStatus.PENDING;
		}

		ParticipationRequest request = ParticipationRequest.builder()
				.requesterId(requester.id())
				.eventId(event.getId())
				.status(status)
				.created(LocalDateTime.now())
				.build();

		UserActionProto userAction = UserActionProto.newBuilder()
				.setUserId(userId)
				.setEventId(eventId)
				.setActionType(ActionTypeProto.ACTION_REGISTER)
				.setTimestamp(Timestamp.newBuilder()
						.setSeconds(Instant.now().getEpochSecond())
						.setNanos(Instant.now().getNano())
						.build())
				.build();

		collectorClient.sendUserAction(userAction);
		return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
	}

	public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
		ParticipationRequest request = getRequestById(requestId);

		if (!request.getRequesterId().equals(userId)) {
			throw new ConflictException("Нельзя отменить чужую заявку");
		}
		request.setStatus(ParticipationStatus.CANCELED);
		return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
	}

	@NonNull
	private UserDto getUserById(long userId) {
		return userClient.getUserById(userId);
	}

	@NonNull
	private EventFullDto getEventById(long eventId) {
		return eventClient.getEventById(eventId);
	}

	@NonNull
	private ParticipationRequest getRequestById(Long requestId) {
		return requestRepository.findById(requestId).orElseThrow(
				() -> new NotFoundException("Заявка с id=" + requestId + " не найдена")
		);
	}

	@Override
	public int countByEventIdAndStatus(Long eventId, ParticipationStatus participationStatus) {
		return requestRepository.countByEventIdAndStatus(eventId, participationStatus);
	}

	@Override
	public List<EventRequestCountDto> countConfirmedRequestsByEventIds(List<Long> eventIds, ParticipationStatus status) {
		return requestRepository.countConfirmedRequestsByEventIds(eventIds, status);
	}

}
