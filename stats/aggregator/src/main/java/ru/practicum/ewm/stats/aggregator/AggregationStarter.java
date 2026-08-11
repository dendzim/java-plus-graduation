package ru.practicum.ewm.stats.aggregator;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.aggregator.service.EventSimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.kafka.deserializer.UserActionDeserializer;
import ru.practicum.ewm.stats.kafka.serializer.AvroSerializer;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class AggregationStarter {

    @Value("${aggregator.kafka.topic.actions}")
    private String userActionsTopic;
    @Value("${aggregator.kafka.topic.similarity}")
    private String eventsSimilarityTopic;
    private final Duration CONSUME_ATTEMPT_TIMEOUT;

    private static final long OFFSET_INCREMENT = 1L;

    private final KafkaConsumer<Void, SpecificRecordBase> consumer;
    private final KafkaProducer<Void, SpecificRecordBase> producer;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    private final EventSimilarityService eventSimilarityService;

    private Properties getProducerConfig(String bootstrapServer) {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, VoidSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class);
        return config;
    }

    private Properties getConsumerConfig(String bootstrapServer) {
        Properties config = new Properties();
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "aggregator.id");
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return config;
    }

    @Autowired
    public AggregationStarter(EventSimilarityService eventSimilarityService,
                              @Value("${aggregator.kafka.bootstrap}") String bootstrapServer,
                              @Value("${aggregator.kafka.consume.attempt.timeout}") long pollTimeoutMs) {
        this.eventSimilarityService = eventSimilarityService;
        this.consumer = new KafkaConsumer<>(getConsumerConfig(bootstrapServer));
        this.producer = new KafkaProducer<>(getProducerConfig(bootstrapServer));
        this.CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(pollTimeoutMs);

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    public void start() {
        try {
            consumer.subscribe(List.of(userActionsTopic));
            while (true) {
                List<UserActionAvro> userActions = pollUserActions();

                if (!userActions.isEmpty()) {
                    for (UserActionAvro userAction : userActions) {
                        List<EventSimilarityAvro> similarities = eventSimilarityService.processUserAction(userAction);

                        for (EventSimilarityAvro similarity : similarities) {
                            sendToSimilarity(similarity);
                        }
                    }
                }
            }

        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Ошибка во время обработки", e);
        } finally {

            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    public List<UserActionAvro> pollUserActions() {
        List<UserActionAvro> messages = new ArrayList<>();
        try {
            ConsumerRecords<Void, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

            for (ConsumerRecord<Void, SpecificRecordBase> record : records) {
                messages.add((UserActionAvro) record.value());

                currentOffsets.put(new TopicPartition(record.topic(), record.partition()),
                        new OffsetAndMetadata(record.offset() + OFFSET_INCREMENT));
            }

            if (!records.isEmpty()) {
                consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                    if (exception != null) {
                        log.warn("Ошибка коммита оффсета: {}", offsets, exception);
                    }
                });
            }
        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("ошибка чтения сообщения из Kafka", e);
        }
        return messages;
    }

    public void sendToSimilarity(EventSimilarityAvro similarityAvro) {
        try {
            producer.send(new ProducerRecord<>(eventsSimilarityTopic, similarityAvro));
        } catch (Exception e) {
            log.error("Ошибка при отправке записи {} в топик {}", similarityAvro, eventsSimilarityTopic, e);

        }
    }

    public void wakeup(){
        this.consumer.wakeup();
    }

}
