package com.jippy.driver.dto.uber;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "uber.direct")
@Data
public class UberConfigProperties {

    private String baseUrl;
    private String authUrl;
    private String customerId;
    private String clientId;
    private String clientSecret;
    private String scope;
}
