package ru.practicum.ewm.stats.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.analyzer.service.EventSimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.kafka.deserializer.EventSimilarityDeserializer;

import java.time.Duration;
import java.util.*;


@Slf4j
@Component
public class EventSimilarityProcessor implements Runnable {

    @Value("${analyzer.kafka.topic.similarity}")
    private String eventsSimilarityTopic;

    private static final long OFFSET_INCREMENT = 1L;
    private final Duration CONSUME_ATTEMPT_TIMEOUT;
    private final KafkaConsumer<Void, SpecificRecordBase> consumer;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final EventSimilarityService similarityService;

    @Autowired
    public EventSimilarityProcessor(EventSimilarityService similarityService,
                             @Value("${analyzer.kafka.bootstrap}") String bootstrapServer,
                             @Value("${analyzer.kafka.consume.attempt.timeout}") long pollTimeoutMs) {
        this.similarityService = similarityService;
        this.consumer = new KafkaConsumer<>(getConsumerConfig(bootstrapServer));
        this.CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(pollTimeoutMs);
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    private Properties getConsumerConfig(String bootstrapServer) {
        Properties config = new Properties();
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer-group-similarity");
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return config;
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(List.of(eventsSimilarityTopic));

            while (true) {
                ConsumerRecords<Void, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                if (records.isEmpty()) {
                    continue;
                }
                List<EventSimilarityAvro> messages = new ArrayList<>();

                for (ConsumerRecord<Void, SpecificRecordBase> record : records) {
                    try {
                        messages.add((EventSimilarityAvro) record.value());
                        currentOffsets.put(new TopicPartition(record.topic(), record.partition()),
                                new OffsetAndMetadata(record.offset() + OFFSET_INCREMENT));
                    } catch (Exception e) {
                        log.error("Ошибка обработки события с оффсетом {}", record.offset(), e);
                    }
                }
                try {
                    similarityService.saveSimilarities(messages);

                    if (!currentOffsets.isEmpty()) {
                        consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                            if (exception != null) {
                                log.warn("Ошибка коммита оффсетов: {}", offsets, exception);
                            }
                        });
                    }
                } catch (Exception e) {
                    log.error("Ошибка сохранения схожестей, коммит НЕ выполнен", e);
                }
            }

        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Ошибка обработки", e);
        } finally {

            try {
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Консьюмер закрыт");
                consumer.close();
            }
        }
    }
}
