package com.jippy.foodandmart.configuration;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    private final String bootstrapServers;

    public KafkaProducerConfig(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

        this.bootstrapServers = bootstrapServers;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {

        Map<String, Object> configs = new HashMap<>();

        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Wait for all in-sync replicas to acknowledge
        configs.put(ProducerConfig.ACKS_CONFIG, "all");

        // Enable idempotent publishing
        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // Retry temporary Kafka failures
        configs.put(ProducerConfig.RETRIES_CONFIG, 10);

        // Do not add Java type headers
        configs.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {

        return new KafkaTemplate<>(producerFactory);
    }
}