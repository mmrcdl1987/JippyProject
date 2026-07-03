
package com.jippy.gatewayserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;

@Configuration
@Slf4j
public class GatewayCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        log.info("===================cors called===========");
        //config.addAllowedOrigin("http://localhost:5173"); // Or port 3000 depending on your local Vite/React setup

        // 2. Allow your production Hostinger VPS domain or IP
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://srv1617582.hstgr.cloud:3005"
        ));

        // Allow the exact origin Swagger is running on
        //config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*"); // Allow GET, POST, PUT, DELETE, OPTIONS
        config.addAllowedHeader("*"); // Allow all authentication headers
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}

