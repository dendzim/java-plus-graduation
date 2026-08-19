package ru.practicum.ewm.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.analyzer.mapper.EventSimilarityMapper;
import ru.practicum.ewm.stats.analyzer.model.EventSimilarity;
import ru.practicum.ewm.stats.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EventSimilarityService {

    private final EventSimilarityRepository similarityRepository;
    private final EventSimilarityMapper similarityMapper;

    @Transactional
    public void saveSimilarities(List<EventSimilarityAvro> eventSimilarityAvroList) {
        if (eventSimilarityAvroList == null || eventSimilarityAvroList.isEmpty()) {
            return;
        }

        List<EventSimilarity> similarityToUpdate = new ArrayList<>();
        List<EventSimilarity> similarityToSave = new ArrayList<>();

        for (EventSimilarityAvro avro : eventSimilarityAvroList) {
            similarityRepository.findByEventAAndEventB(avro.getEventA(), avro.getEventB())
                    .ifPresentOrElse(
                            eventSimilarity -> similarityToUpdate.add(updateSimilarity(eventSimilarity, avro)),
                            () -> similarityToSave.add(similarityMapper.toEventSimilarity(avro))
                    );
        }

        if (!similarityToUpdate.isEmpty()) {
            similarityRepository.saveAll(similarityToUpdate);
        }

        if (!similarityToSave.isEmpty()) {
            similarityRepository.saveAll(similarityToSave);
        }
    }

    private EventSimilarity updateSimilarity(EventSimilarity eventSimilarity, EventSimilarityAvro avro) {
        eventSimilarity.setScore(avro.getScore());
        eventSimilarity.setTimestamp(avro.getTimestamp());
        return eventSimilarity;
    }

    public List<EventSimilarity> findByEventIdIn(Collection<Long> ids) {
        return similarityRepository.findByEventIdIn(ids);
    }

    public List<EventSimilarity> findByEventIdOrderByScore(Long eventId, Integer limit) {
        return similarityRepository.findByEventIdOrderByScoreDesc(eventId, limit);
    }
}
