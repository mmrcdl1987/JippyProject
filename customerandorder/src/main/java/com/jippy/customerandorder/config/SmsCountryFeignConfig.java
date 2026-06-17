package com.jippy.customerandorder.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class SmsCountryFeignConfig {

    @Bean
    public RequestInterceptor smsCountryRequestInterceptor(SmsCountryProperties properties) {

        return template -> {

            String auth = properties.getAuthKey() + ":" + properties.getAuthToken();

            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            template.header("Authorization", "Basic " + encodedAuth);

            template.header("Content-Type", "application/json");
        };
    }
}