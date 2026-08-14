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
import ru.practicum.ewm.stats.analyzer.service.UserActionService;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.kafka.deserializer.UserActionDeserializer;

import java.time.Duration;
import java.util.*;


@Slf4j
@Component
public class UserActionProcessor {

    @Value("${analyzer.kafka.topic.actions}")
    private String userActionsTopic;

    private static final long OFFSET_INCREMENT = 1L;
    private final Duration CONSUME_ATTEMPT_TIMEOUT;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaConsumer<Void, SpecificRecordBase> consumer;
    private final UserActionService userActionService;

    @Autowired
    public UserActionProcessor(UserActionService userActionService,
                             @Value("${analyzer.kafka.bootstrap}") String bootstrapServer,
                             @Value("${analyzer.kafka.consume.attempt.timeout}") long pollTimeoutMs) {
        this.userActionService = userActionService;
        this.consumer = new KafkaConsumer<>(getConsumerConfig(bootstrapServer));
        this.CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(pollTimeoutMs);
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    private Properties getConsumerConfig(String bootstrapServer) {
        Properties config = new Properties();
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer-group-actions");
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return config;
    }

    public void start() {
        try {
            consumer.subscribe(List.of(userActionsTopic));

            while (true) {
                ConsumerRecords<Void, SpecificRecordBase> consumerRecords =
                        consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                if (consumerRecords.isEmpty()) {
                    continue;
                }

                List<UserActionAvro> messages = new ArrayList<>();
                Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();

                for (TopicPartition partition : consumerRecords.partitions()) {
                    List<ConsumerRecord<Void, SpecificRecordBase>> partitionRecords =
                            consumerRecords.records(partition);

                    for (ConsumerRecord<Void, SpecificRecordBase> record : partitionRecords) {
                        if (record.value() instanceof UserActionAvro) {
                            messages.add((UserActionAvro) record.value());
                        } else {
                            log.warn("Пропущено сообщение типа: {}",
                                    record.value().getClass().getName());
                        }
                    }

                    long nextOffset = partitionRecords.getLast().offset() + OFFSET_INCREMENT;
                    offsetsToCommit.put(partition, new OffsetAndMetadata(nextOffset));
                }

                try {
                    if (!messages.isEmpty()) {
                        userActionService.saveUserAction(messages);
                    }

                    if (!offsetsToCommit.isEmpty()) {
                        consumer.commitSync(offsetsToCommit);
                    }
                    log.info("Обработано {} сообщений, коммит выполнен", messages.size());

                } catch (Exception e) {
                    log.error("Ошибка сохранения {} сообщений, коммит не выполнен",
                            messages.size(), e);
                }
            }
        } catch (WakeupException ignored) {
            log.info("Получен сигнал завершения");
        } catch (Exception e) {
            log.error("Критическая ошибка", e);
        } finally {
            try {
                consumer.commitSync();
            } catch (Exception e) {
                log.error("Ошибка коммита", e);
            } finally {
                consumer.close();
                log.info("Консьюмер закрыт");
            }
        }
    }
}
