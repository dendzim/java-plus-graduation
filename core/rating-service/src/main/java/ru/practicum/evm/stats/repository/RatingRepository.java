package ru.practicum.evm.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.evm.stats.model.Rating;
import ru.practicum.evm.stats.enums.Reaction;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

	Optional<Rating> findByUserIdAndEventId(Long userId, Long eventId);

	boolean existsByUserIdAndEventId(Long userId, Long eventId);

	long countByEventIdAndReaction(Long eventId, Reaction reaction);
}
