package com.jippy.notification.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

@Configuration
@Slf4j
public class NKafkaConfig {

    /*@Bean
    public RecordMessageConverter converter() {
        return new StringJsonMessageConverter();
    }*/

    @Bean
    public RecordMessageConverter converter() {
        log.info("CONFIG_START | KAFKA_MESSAGE_CONVERTER");
        StringJsonMessageConverter converter = new StringJsonMessageConverter();
        // This tells the converter which packages it is allowed to deserialize
        // You can also use a Map to map the Producer's class name to your Consumer's class name
        log.info("CONFIG_END | KAFKA_MESSAGE_CONVERTER_INITIALIZED");
        return converter;
    }
}