package ru.practicum.ewm.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.stats.analyzer.model.UserAction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {
    Optional<UserAction> findByUserIdAndEventId(long userId, long eventId);

    List<UserAction> findByUserIdOrderByTimestampDesc(Long userId);

    Collection<UserAction> findAllByEventId(Long eventId);
}
