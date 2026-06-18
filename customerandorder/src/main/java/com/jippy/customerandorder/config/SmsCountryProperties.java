package com.jippy.customerandorder.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "smscountry")
public class SmsCountryProperties {

    private String authKey;

    private String authToken;

    private String senderId;

    private String baseUrl;
}