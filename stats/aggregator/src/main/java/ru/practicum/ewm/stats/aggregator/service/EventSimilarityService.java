package ru.practicum.ewm.stats.aggregator.service;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;

public class EventSimilarityService {

    /// Общая сумма весов для каждого события
    private final Map<Long, Double> eventWeightSums = new HashMap<>();

    /// Максимальный вес каждого пользователя для каждого события
    private final Map<Long, Map<Long, Double>> eventUserMaxWeights = new HashMap<>();

    /// Сумма минимальных весов для каждой пары событий
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    public List<EventSimilarityAvro> processUserAction(UserActionAvro userAction) {
        long eventId = userAction.getEventId();
        long userId = userAction.getUserId();

        double newWeight = computeWeightActionType(userAction.getActionType());
        double oldWeight = getWeight(eventId, userId);

        //если вес не стал больше
        if (newWeight <= oldWeight) {
            return Collections.emptyList();
        }


        eventUserMaxWeights.computeIfAbsent(eventId, k -> new HashMap<>())
                .put(userId, newWeight);

        double difference = newWeight - oldWeight;
        double newSum = eventWeightSums.getOrDefault(eventId, 0.0) + difference;
        eventWeightSums.put(eventId, newSum);

        return calculateSimilarities(eventId, userId, oldWeight, newWeight, userAction.getTimestamp());
    }

    private double getWeight(long eventId, long userId) {
        Map<Long, Double> weights = eventUserMaxWeights.get(eventId);
        return (weights != null) ? weights.getOrDefault(userId, 0.0) : 0.0;
    }

    private double computeWeightActionType(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

    private List<EventSimilarityAvro> calculateSimilarities(long eventId, long userId,
                                                            double oldWeight, double newWeight,
                                                            Instant timestamp) {

        List<EventSimilarityAvro> results = new ArrayList<>();

        for (long otherEventId : eventUserMaxWeights.keySet()) {
            if (otherEventId == eventId) continue;

            Map<Long, Double> otherWeights = eventUserMaxWeights.get(otherEventId);
            Double otherWeight = (otherWeights != null) ? otherWeights.get(userId) : null;

            if (otherWeight == null) continue;

            double similarity = updateAndCalculateSimilarity(eventId, otherEventId, oldWeight, newWeight, otherWeight);

            if (similarity > 0) {
                long first = Math.min(eventId, otherEventId);
                long second = Math.max(eventId, otherEventId);

                //собираем сообщение для отправки
                EventSimilarityAvro eventSimilarityAvro = EventSimilarityAvro.newBuilder()
                        .setEventA(first)
                        .setEventB(second)
                        .setScore(similarity)
                        .setTimestamp(timestamp)
                        .build();

                results.add(eventSimilarityAvro);
            }
        }

        return results;
    }

    private double updateAndCalculateSimilarity(long eventId1, long eventId2,
                                      double oldWeight, double newWeight, double otherWeight) {

        long first = Math.min(eventId1, eventId2);
        long second = Math.max(eventId1, eventId2);

        Map<Long, Double> pairSums = minWeightsSums.computeIfAbsent(first, k -> new HashMap<>());
        double currentMin = pairSums.getOrDefault(second, 0.0);

        double oldMin = Math.min(oldWeight, otherWeight);
        double newMin = Math.min(newWeight, otherWeight);
        double diff = newMin - oldMin;

        pairSums.put(second, currentMin + diff);

        Double sum1 = eventWeightSums.get(first);
        Double sum2 = eventWeightSums.get(second);

        if (sum1 == null || sum2 == null || sum1 <= 0 || sum2 <= 0) {
            return 0.0;
        }

        double sMin = pairSums.get(second);
        return sMin / Math.sqrt(sum1 * sum2);
    }
}
