package ru.practicum.evm.stats.service;

import ru.practicum.evm.stats.dto.rating.RatingRequest;
import ru.practicum.evm.stats.dto.rating.RatingResponse;

public interface RatingService {

	RatingResponse addOrUpdateReaction(Long userId, Long eventId, RatingRequest request);

	void removeReaction(Long userId, Long eventId);
}
