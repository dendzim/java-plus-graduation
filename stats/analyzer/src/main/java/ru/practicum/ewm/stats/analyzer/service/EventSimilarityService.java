package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.analyzer.mapper.EventSimilarityMapper;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarity;
import ru.practicum.ewm.stats.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EventSimilarityService {

    private final EventSimilarityRepository similarityRepository;
    private final EventSimilarityMapper similarityMapper;


    public void saveSimilarities(List<EventSimilarityAvro> eventSimilarityAvroList) {
        for (EventSimilarityAvro avro : eventSimilarityAvroList) {
            similarityRepository.findByEventAAndEventB(avro.getEventA(), avro.getEventB())
                    .ifPresentOrElse(
                            eventSimilarity -> updateSimilarity(eventSimilarity, avro),
                            () -> similarityRepository.save(similarityMapper.toEventSimilarity(avro))
                    );
        }
    }

    private void updateSimilarity(EventSimilarity eventSimilarity, EventSimilarityAvro avro) {
        eventSimilarity.setScore(avro.getScore());
        eventSimilarity.setTimestamp(avro.getTimestamp());
        similarityRepository.save(eventSimilarity);
    }

    public List<EventSimilarity> findByEventIdIn(Collection<Long> ids) {
        return similarityRepository.findByEventIdIn(ids);
    }

    public List<EventSimilarity> findByEventIdOrderByScore(Long eventId, Integer limit) {
        return similarityRepository.findByEventIdOrderByScoreDesc(eventId, limit);
    }
}
