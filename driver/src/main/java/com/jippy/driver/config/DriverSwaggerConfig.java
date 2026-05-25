package com.jippy.driver.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DriverSwaggerConfig {

    // Reads GATEWAY_SWAGGER_URL from system/docker environment. Falls back to localhost:8060 if missing.
    @Value("${gateway.swagger.url:http://localhost:8084}")
    private String gatewaySwaggerUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Authorization";

        return new OpenAPI()
                // FORCE SWAGGER UI TO CALL THE GATEWAY AND INCLUDE THE /api/fm ROUTING PREFIX
                .servers(List.of(
                        new Server().url(gatewaySwaggerUrl).description("API Gateway Server")
                ))
                // 1. Link the requirement globally so every endpoint shows a lock icon
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // 2. Define the security scheme structure (HTTP Bearer using JWT)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        // FIX 2: Using APIKEY type instead of HTTP tells Swagger
                                        // to transmit your exact input string without prepending "Bearer "
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                                        .description("Paste your raw token here directly (WITHOUT the word Bearer)")
                        ));
    }}
