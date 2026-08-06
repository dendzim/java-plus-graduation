package ru.practicum.service;

import ru.practicum.dto.rating.RatingRequest;
import ru.practicum.dto.rating.RatingResponse;

public interface RatingService {

	RatingResponse addOrUpdateReaction(Long userId, Long eventId, RatingRequest request);

	void removeReaction(Long userId, Long eventId);
}
