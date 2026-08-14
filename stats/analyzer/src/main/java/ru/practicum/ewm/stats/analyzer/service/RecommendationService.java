package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.analyzer.model.ActionType;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarity;
import ru.practicum.ewm.stats.analyzer.model.UserAction;
import ru.practicum.grpc.stats.action.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.action.RecommendedEventProto;
import ru.practicum.grpc.stats.action.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.action.UserPredictionsRequestProto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final EventSimilarityService eventSimilarityService;
    private final UserActionService userActionService;

    public Stream<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto requestProto) {

        List<UserAction> userActions = userActionService.findByUserIdOrderByTimestamp(requestProto.getUserId());

        if (userActions.isEmpty()) {
            return Stream.empty();
        }

        Set<Long> userEventIds = userActions.stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        Map<Long, Double> eventRatings = userActions.stream()
                .collect(Collectors.toMap(
                        UserAction::getEventId,
                        userAction -> getWeight(userAction.getActionType()),
                        Math::max
                ));

        List<EventSimilarity> similarities = eventSimilarityService.findByEventIdIn(userEventIds);

        Map<Long, Double> candidateScores = new HashMap<>();

        for (EventSimilarity eventSimilarity : similarities) {
            Long candidateId = extractCandidate(eventSimilarity, userEventIds);
            if (candidateId == null) continue;

            Long userEventId = getOppositeEventId(eventSimilarity, candidateId);

            Double weight = eventRatings.get(userEventId);
            if (weight != null) {
                double score = weight * eventSimilarity.getScore();
                candidateScores.merge(candidateId, score, Math::max);
            }
        }

        return candidateScores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(requestProto.getMaxResults())
                .map(entry -> assembleRecommendedEvent(entry.getKey(), entry.getValue()));

    }

    private Long extractCandidate(EventSimilarity eventSimilarity, Set<Long> userEventIds) {
        if (userEventIds.contains(eventSimilarity.getEventA())) {
            return eventSimilarity.getEventB();
        } else if (userEventIds.contains(eventSimilarity.getEventB())) {
            return eventSimilarity.getEventA();
        }
        return null;
    }

    public Stream<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto requestProto) {
        return eventSimilarityService.findByEventIdOrderByScore(requestProto.getEventId(),
                        requestProto.getMaxResults())
                .stream()
                .map(eventSimilarity -> {
                    Long otherId = getOppositeEventId(eventSimilarity, requestProto.getEventId());
                    return assembleRecommendedEvent(otherId, eventSimilarity.getScore());
                });
    }

    private Long getOppositeEventId(EventSimilarity eventSimilarity, Long id) {
        if (eventSimilarity == null || id == null) {
            return null;
        }

        Long eventAId = eventSimilarity.getEventA();
        Long eventBId = eventSimilarity.getEventB();

        if (eventAId == null || eventBId == null) {
            return null;
        }

        if (eventAId.equals(id)) {
            return eventBId;
        } else if (eventBId.equals(id)) {
            return eventAId;
        }
        return null;
    }

    public Stream<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto requestProto) {
        return requestProto.getEventIdList().stream()
                .distinct()
                .map(eventId -> {
                    Double sum = userActionService.calculateMaxRatingPerUserByEventId(eventId);
                    return assembleRecommendedEvent(eventId, sum);
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()));
    }

    private RecommendedEventProto assembleRecommendedEvent(Long eventId, Double score) {
        return RecommendedEventProto.newBuilder()
                .setEventId(eventId)
                .setScore(score)
                .build();
    }

    public double getWeight(ActionType actionType) {
        return switch (actionType) {
            case ActionType.VIEW -> 0.4;
            case ActionType.REGISTER -> 0.8;
            case ActionType.LIKE -> 1.0;
        };
    }
}
