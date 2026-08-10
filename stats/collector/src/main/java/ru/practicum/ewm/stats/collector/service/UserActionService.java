package ru.practicum.ewm.stats.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.collector.kafka.KafkaClient;
import ru.practicum.ewm.stats.collector.mapper.UserActionMapper;
import ru.practicum.grpc.stats.action.UserActionProto;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionService {

    @Value("${collector.kafka.topic.actions}")
    private String topicUserActions;

    private final KafkaClient client;

    public void collectUserAction(UserActionProto request) {
        try {
            Producer<String, SpecificRecordBase> producer = client.getProducer();

            UserActionAvro avro = UserActionMapper.toAvro(request);
            producer.send(new ProducerRecord<>(topicUserActions, avro));
            log.info("Отправлено действие пользователя в Kafka: userId={}, eventId={}, actionType={}",
                    avro.getUserId(),
                    avro.getEventId(),
                    avro.getActionType());

        } catch (Exception e) {
            log.error("Ошибка отправки действия пользователя в Kafka", e);
            throw new RuntimeException("Failed to send user action to Kafka", e);
        }
    }
}