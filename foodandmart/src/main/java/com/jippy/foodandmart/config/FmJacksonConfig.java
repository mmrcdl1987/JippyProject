package com.jippy.foodandmart.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Global Jackson configuration.
 *
 * The bare ObjectMapper() that was here before had NO modules registered,
 * which overrode Spring Boot's auto-configured one and caused:
 *   "Java 8 date/time type LocalDateTime not supported by default"
 *
 * Fix: register JavaTimeModule so ALL LocalDateTime fields across every
 * entity, DTO and response object serialise correctly — no per-class
 * annotations needed.
 */
@Configuration
public class FmJacksonConfig {

    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // Use ISO-8601 string format for LocalDateTime everywhere
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(formatter));

        return new ObjectMapper()
                .registerModule(javaTimeModule)
                // Do NOT write dates as [2026,4,11,10,30,0] arrays
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
