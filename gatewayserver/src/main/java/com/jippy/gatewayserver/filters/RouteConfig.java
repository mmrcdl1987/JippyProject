package com.jippy.gatewayserver.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Slf4j
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, AuthenticationFilter authFilter) {
       log.info("DEBUG: Setting up routes in RouteConfig...");
        return builder.routes()
                // 1. Auth Service Route (Permit All)
                .route("auth-service", r -> r.path("/auth/**")
                        .filters(f -> {
                            log.info("DEBUG: Routing to Auth Service...");
                            return f;
                        })
                        .uri("lb://FOODANDMART"))

                // 2. Protected FoodAndMart Route
                .route("foodandmart-protected", r -> r.path("/api/fm/**")
                        .filters(f -> {
                           log.info(" Applying AuthenticationFilter to /api/fm request...");
                            // This manually applies your custom filter
                            return f.filter(authFilter.apply(new AuthenticationFilter.Config()));
                        })
                        .uri("lb://FOODANDMART"))

                // 23. Protected CustomerAndOrder Route
                .route("customerandorder-protected", r -> r.path("/api/co/**")
                .filters(f -> {
                    log.info("Applying AuthenticationFilter to /api/co request...");
                    // This manually applies your custom filter
                    return f.filter(authFilter.apply(new AuthenticationFilter.Config()));
                })
                .uri("lb://CUSTOMERANDORDER"))
                .build();
    }
}
