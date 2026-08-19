package ru.practicum.ewm.stats.collector.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.kafka.serializer.AvroSerializer;

import java.util.Properties;

@Component
public class KafkaClient {

    @Value("${collector.kafka.producer.properties.bootstrap.servers}")
    private String bootstrapServer;

    private Producer<String, SpecificRecordBase> producer;

    public Producer<String, SpecificRecordBase> getProducer() {
        if (producer == null) {
            initProducer();
        }
        return producer;
    }

    private void initProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class);

        producer = new org.apache.kafka.clients.producer.KafkaProducer<>(config);

    }

    public void stop() {
        if (producer != null) {
            producer.close();
        }
    }
}
