package com.jippy.gatewayserver.securty;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)// Ensure this config loads before any defaults
    @Bean
    @Primary
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                // Explicitly permit GET & OPTIONS requests for the WebSocket handshake
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/ws-group-order", "/ws-group-order/**").permitAll()
                        .anyExchange().permitAll() // Allow EVERYTHING temporarily
                )
                .build();
    }
}
