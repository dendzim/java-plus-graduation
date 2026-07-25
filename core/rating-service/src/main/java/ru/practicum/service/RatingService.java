package ru.practicum.service;

import ru.practicum.dto.RatingRequest;
import ru.practicum.dto.RatingResponse;

public interface RatingService {

	RatingResponse addOrUpdateReaction(Long userId, Long eventId, RatingRequest request);

	void removeReaction(Long userId, Long eventId);
}
