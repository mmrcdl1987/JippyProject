package com.jippy.notification.configuration;

import com.jippy.notification.dto.NWalletPointsEvent;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;


/**
 * Kafka Consumer Configuration for Wallet Points Events.
 *
 * This configuration is used only for the
 * "wallet-points-earned" Kafka topic.
 *
 * It tells Spring Kafka how to:
 *
 * 1. Connect to the Kafka server.
 * 2. Read messages from the wallet-points-earned topic.
 * 3. Convert the incoming JSON message into NWalletPointsEvent.
 * 4. Provide a Kafka listener factory for NWalletPointsConsumer.
 */
@Configuration
public class NWalletPointsKafkaConsumerConfig {


    /*
     * Reads the Kafka server address from application.yml.
     *
     * Example:
     *
     * spring:
     *   kafka:
     *     bootstrap-servers: 187.xxx.xxx.xxx:9092
     */
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


    /**
     * Creates a dedicated Kafka ConsumerFactory
     * for NWalletPointsEvent.
     *
     * ConsumerFactory is responsible for creating
     * Kafka consumers with the required configuration.
     *
     * Most importantly, this factory tells Kafka:
     *
     * Kafka JSON
     *      ↓
     * NWalletPointsEvent
     *
     * This prevents the wallet-points event from being
     * incorrectly converted into NOrderEvent.
     */
    @Bean
    public ConsumerFactory<String, NWalletPointsEvent> walletPointsConsumerFactory() {

        /*
         * Store Kafka consumer configuration properties.
         */
        Map<String, Object> properties = new HashMap<>();


        /*
         * Kafka server address.
         *
         * This tells the consumer where the Kafka broker
         * is running.
         */
        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );


        /*
         * Consumer group for wallet points notifications.
         *
         * Kafka uses this group to keep track of which
         * wallet-point messages have already been consumed.
         */
        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "notification-wallet-points-group"
        );


        /*
         * If this consumer group does not have an existing
         * offset, start reading messages from the beginning.
         */
        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );


        /*
         * Kafka message key is a String.
         *
         * Example:
         *
         * ORDWP001
         */
        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );


        /*
         * JsonDeserializer converts the JSON received from Kafka
         * into the NWalletPointsEvent Java object.
         *
         * Without this, Spring would not know which DTO
         * should represent the incoming JSON.
         */
        JsonDeserializer<NWalletPointsEvent> jsonDeserializer =
                new JsonDeserializer<>(
                        NWalletPointsEvent.class
                );


        /*
         * Allow NWalletPointsEvent to be deserialized because
         * it belongs to this trusted package.
         */
        jsonDeserializer.addTrustedPackages(
                "com.jippy.notification.dto"
        );


        /*
         * Ignore Java type information that may be present
         * in Kafka message headers.
         *
         * This is important because the producer is using:
         *
         * CoWalletPointsEvent
         *
         * while Notification MS uses:
         *
         * NWalletPointsEvent
         *
         * Both DTOs have the same JSON structure.
         *
         * Therefore Notification MS should use its own DTO
         * instead of trying to use the producer's DTO class.
         */
        jsonDeserializer.setUseTypeHeaders(false);


        /*
         * Create and return the ConsumerFactory.
         *
         * StringDeserializer
         *      → converts Kafka key into String
         *
         * jsonDeserializer
         *      → converts Kafka JSON into NWalletPointsEvent
         */
        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                jsonDeserializer
        );
    }


    /**
     * Creates the Kafka Listener Container Factory
     * for NWalletPointsEvent.
     *
     * This factory is used by:
     *
     * NWalletPointsConsumer
     *
     * through:
     *
     * containerFactory =
     * "walletPointsKafkaListenerContainerFactory"
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String, NWalletPointsEvent>
    walletPointsKafkaListenerContainerFactory() {

        /*
         * Create the listener container factory.
         *
         * String
         *      → Kafka key
         *
         * NWalletPointsEvent
         *      → Kafka message value
         */
        ConcurrentKafkaListenerContainerFactory<
                String,
                NWalletPointsEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();


        /*
         * Attach our dedicated ConsumerFactory.
         *
         * This means the listener will use the
         * NWalletPointsEvent JSON deserializer configured above.
         */
        factory.setConsumerFactory(
                walletPointsConsumerFactory()
        );


        /*
         * Return the configured factory.
         *
         * Spring will use this factory whenever a
         * @KafkaListener specifies:
         *
         * containerFactory =
         * "walletPointsKafkaListenerContainerFactory"
         */
        return factory;
    }
}