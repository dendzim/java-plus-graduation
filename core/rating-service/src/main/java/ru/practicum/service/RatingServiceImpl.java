package ru.practicum.service;

import com.google.protobuf.Timestamp;
import feign.FeignException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.feignClient.EventClient;
import ru.practicum.feignClient.UserClient;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.grpc.stats.action.UserActionProto;
import ru.practicum.repository.RatingRepository;
import ru.practicum.dto.rating.RatingRequest;
import ru.practicum.dto.rating.RatingResponse;
import ru.practicum.model.Rating;
import ru.practicum.enums.EventState;
import ru.practicum.enums.Reaction;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.stat.client.CollectorClient;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingServiceImpl implements RatingService {

	private final RatingRepository ratingRepository;
	private final UserClient userClient;
	private final EventClient eventClient;
	private final CollectorClient collectorClient;

	@Override
	public RatingResponse addOrUpdateReaction(Long userId, Long eventId, RatingRequest request) {
		UserDto user = userClient.getUserById(userId);
		EventFullDto event;
		try {
			event = eventClient.findByIdAndState(eventId, EventState.PUBLISHED);
		} catch (FeignException.NotFound e) {
			throw new NotFoundException("Событие с ID " + eventId + " не найдено или не опубликовано");
		} catch (FeignException e) {
			throw new ServiceException("Сервис событий временно недоступен");
		}
		Long initiatorId = event.getInitiator() != null ? event.getInitiator().id() : null;
		if (initiatorId == null) {
			throw new ValidationException("У события отсутствует инициатор");
		}
		if (Objects.equals(user.id(), initiatorId)) {
			throw new ValidationException("Нельзя ставить реакции своим событиям");
		}

		Rating rating = ratingRepository.findByUserIdAndEventId(userId, eventId).orElse(null);

		if (rating != null) {
			if (rating.getReaction() == request.getReaction()) {
				ratingRepository.delete(rating);
				updateEventRate(event);
				throw new ConflictException("Reaction removed");
			} else {
				rating.setReaction(request.getReaction());
				ratingRepository.save(rating);
				updateEventRate(event);
				if (request.getReaction() == Reaction.LIKE) {
					UserActionProto userAction = UserActionProto.newBuilder()
							.setUserId(userId)
							.setEventId(eventId)
							.setActionType(ActionTypeProto.ACTION_LIKE)
							.setTimestamp(Timestamp.newBuilder()
									.setSeconds(Instant.now().getEpochSecond())
									.setNanos(Instant.now().getNano())
									.build())
							.build();

					collectorClient.sendUserAction(userAction);
				}
				return mapToResponse(rating);
			}
		} else {
			rating = Rating.builder()
					.userId(userId)
					.eventId(eventId)
					.reaction(request.getReaction())
					.build();
			ratingRepository.save(rating);
			updateEventRate(event);
			return mapToResponse(rating);
		}
	}

	@Override
	public void removeReaction(Long userId, Long eventId) {
		Rating rating = ratingRepository.findByUserIdAndEventId(userId, eventId)
				.orElseThrow(() -> new NotFoundException("Реакция не найдена"));
		ratingRepository.delete(rating);
		EventFullDto event = eventClient.getEventById(rating.getEventId());
		updateEventRate(event);
	}

	private void updateEventRate(@NonNull EventFullDto event) {
		long likes = ratingRepository.countByEventIdAndReaction(event.getId(), Reaction.LIKE);
		long dislikes = ratingRepository.countByEventIdAndReaction(event.getId(), Reaction.DISLIKE);

		event.setRate(likes - dislikes);

		eventClient.updateEventRate(event);
	}

	private RatingResponse mapToResponse(@NonNull Rating rating) {
		return RatingResponse.builder()
				.id(rating.getId())
				.userId(rating.getUserId())
				.eventId(rating.getEventId())
				.reaction(rating.getReaction())
				.build();
	}
}
