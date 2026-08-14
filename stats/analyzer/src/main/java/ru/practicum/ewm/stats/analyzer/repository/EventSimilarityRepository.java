package ru.practicum.ewm.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    Optional<EventSimilarity> findByEventAAndEventB(Long eventA, Long eventB);

    @Query("""
            SELECT es
            FROM EventSimilarity es
            WHERE es.eventA = :eventId
            OR es.eventB = :eventId
            ORDER BY es.score DESC
            LIMIT :limit
            """)
    List<EventSimilarity> findByEventIdOrderByScoreDesc(Long eventId, int limit);

    @Query("""
            SELECT es
            FROM EventSimilarity es
            WHERE es.eventA IN :eventIds
            OR es.eventB IN :eventIds
            """)
    List<EventSimilarity> findByEventIdIn(Collection<Long> eventIds);
}
