package ru.practicum.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UserDto;
import ru.practicum.feignClient.EventClient;
import ru.practicum.feignClient.UserClient;
import ru.practicum.repository.RatingRepository;
import ru.practicum.dto.RatingRequest;
import ru.practicum.dto.RatingResponse;
import ru.practicum.model.Rating;
import ru.practicum.enums.EventState;
import ru.practicum.enums.Reaction;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingServiceImpl implements RatingService {

	private final RatingRepository ratingRepository;
	private final UserClient userClient;
	private final EventClient eventClient;

	@Override
	public RatingResponse addOrUpdateReaction(Long userId, Long eventId, RatingRequest request) {
		UserDto user = userClient.getUserById(userId);
		EventFullDto event = eventClient.findByIdAndState(eventId, EventState.PUBLISHED);
		Long initiatorId = event.initiator().id();
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
		long likes = ratingRepository.countByEventIdAndReaction(event.id(), Reaction.LIKE);
		long dislikes = ratingRepository.countByEventIdAndReaction(event.id(), Reaction.DISLIKE);

		EventFullDto updatedEvent = new EventFullDto(
				event.id(), event.annotation(), event.category(),
				event.confirmedRequests(), event.createdOn(), event.description(),
				event.eventDate(), event.initiator(), event.location(),
				event.paid(), event.participantLimit(), event.publishedOn(),
				event.requestModeration(), event.state(), event.title(),
				event.views(), likes - dislikes
		);

		eventClient.updateEventRate(updatedEvent.id());
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
