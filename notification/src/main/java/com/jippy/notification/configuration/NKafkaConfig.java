package com.jippy.notification.configuration;

import com.jippy.notification.dto.*;
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
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class NKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:187.127.156.147:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:notification-group}")
    private String defaultGroupId;

    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> targetType, String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId != null ? groupId : defaultGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        JsonDeserializer<T> jsonDeserializer = new JsonDeserializer<>(targetType);
        jsonDeserializer.addTrustedPackages("com.jippy.notification.dto", "*");
        jsonDeserializer.setUseTypeHeaders(false);

        ErrorHandlingDeserializer<T> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                errorHandlingDeserializer
        );
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createListenerContainerFactory(
            Class<T> targetType, String groupId) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createConsumerFactory(targetType, groupId));
        // Retry 3 times with 2-second interval before giving up / logging
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(2000L, 3)));
        return factory;
    }

    // 1. Order Events Factory
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NOrderEvent> orderKafkaListenerContainerFactory() {
        return createListenerContainerFactory(NOrderEvent.class, defaultGroupId);
    }

    // 2. Wallet Points Events Factory
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NWalletPointsEvent> walletPointsKafkaListenerContainerFactory() {
        return createListenerContainerFactory(NWalletPointsEvent.class, "notification-wallet-points-group");
    }

    // 3. Cart Reminder Events Factory
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NCartReminderDto> cartReminderKafkaListenerContainerFactory() {
        return createListenerContainerFactory(NCartReminderDto.class, "notification-cart-group");
    }

    // 4. Meal Reminder Events Factory
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NMealReminderDto> mealReminderKafkaListenerContainerFactory() {
        return createListenerContainerFactory(NMealReminderDto.class, "notification-meal-group");
    }

    // 5. Profile Incomplete Events Factory
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CoProfileIncompleteCustomer> profileIncompleteKafkaListenerContainerFactory() {
        return createListenerContainerFactory(CoProfileIncompleteCustomer.class, "profile_incomplete_notification_group");
    }

    // 6. Welcome Coupon Events Factory
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, WelcomeCouponNotificationEvent> welcomeCouponKafkaListenerContainerFactory() {
        return createListenerContainerFactory(WelcomeCouponNotificationEvent.class, "welcome-coupon-group");
    }

    // 7. Group Order Events Factory
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, GroupOrderEventDto> groupOrderKafkaListenerContainerFactory() {
        return createListenerContainerFactory(GroupOrderEventDto.class, "group_order_fcm_consumers");
    }
}