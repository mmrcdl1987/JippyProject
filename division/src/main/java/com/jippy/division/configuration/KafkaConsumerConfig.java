package com.jippy.division.configuration;

import com.jippy.division.dto.PromotionEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@Slf4j
public class KafkaConsumerConfig {

    private final String bootstrapServers;
    private final String groupId;

    public KafkaConsumerConfig(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers, @Value("${spring.kafka.consumer.group-id}") String groupId) {

        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
    }

    @Bean
    public ConsumerFactory<String, PromotionEvent> consumerFactory() {

        JsonDeserializer<PromotionEvent> jsonDeserializer = new JsonDeserializer<>(PromotionEvent.class);

        jsonDeserializer.addTrustedPackages("com.jippy.division.dto");

        // Deserialize directly into PromotionEvent
        jsonDeserializer.setUseTypeHeaders(false);
        jsonDeserializer.setUseTypeMapperForKey(false);

        Map<String, Object> configs = new HashMap<>();

        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        configs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {

        ConsumerRecordRecoverer recoverer = (record, exception) -> {

            log.error("[KAFKA] Retries exhausted | topic={} | " + "partition={} | offset={} | value={}", record.topic(), record.partition(), record.offset(), record.value(), exception);
        };

        FixedBackOff fixedBackOff = new FixedBackOff(1_000L, 3L);

        return new DefaultErrorHandler(recoverer, fixedBackOff);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PromotionEvent> kafkaListenerContainerFactory(ConsumerFactory<String, PromotionEvent> consumerFactory, DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, PromotionEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }
}