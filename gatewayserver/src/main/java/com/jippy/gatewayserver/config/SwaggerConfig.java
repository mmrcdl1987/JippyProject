
package com.jippy.gatewayserver.config;


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
public class SwaggerConfig {

    // Reads GATEWAY_SWAGGER_URL from system/docker environment. Falls back to localhost:8060 if missing.
    @Value("${gateway.swagger.url:http://localhost:8084}")
    private String gatewaySwaggerUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Authorization";

        return new OpenAPI()
                // FORCE SWAGGER TO USE GATEWAY URL PORT 8084 INSTEAD OF DOWNSTREAM PORTS
                .servers(List.of(new Server().url(gatewaySwaggerUrl).description("Gateway Server")))

                // Your existing auth configs
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
    }

