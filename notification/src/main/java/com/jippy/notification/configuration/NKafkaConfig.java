package com.jippy.notification.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

@Configuration
public class NKafkaConfig {

    /*@Bean
    public RecordMessageConverter converter() {
        return new StringJsonMessageConverter();
    }*/

    @Bean
    public RecordMessageConverter converter() {
        StringJsonMessageConverter converter = new StringJsonMessageConverter();
        // This tells the converter which packages it is allowed to deserialize
        // You can also use a Map to map the Producer's class name to your Consumer's class name
        return converter;
    }
}