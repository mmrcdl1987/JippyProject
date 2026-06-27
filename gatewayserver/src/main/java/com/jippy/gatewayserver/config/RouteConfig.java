package com.jippy.gatewayserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
@Slf4j
public class RouteConfig     {

    /*@Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, AuthenticationFilter authFilter) {
       log.info("DEBUG: Setting up routes in RouteConfig...");
        return builder.routes()

                // 1. Explicitly let Swagger UI resources pass through without routing modifications
                .route("swagger-ui-assets", r -> r.path("/swagger-ui/**", "/v3/api-docs/**", "/webjars/**")
                        .uri("forward:/")) // Keeps it internal to the gateway server

                // 1. Auth Service Route (Permit All)
                .route("auth-service", r -> r.path("/auth/**")
                        .filters(f -> {
                            log.info("DEBUG: Routing to Auth Service...");
                            return f;
                        })
                        .uri("lb://FOODANDMART"))



                .route("foodandmart-docs", r -> r.path("/api/fm/v3/api-docs")
                        // Notice: NO AuthenticationFilter applied here!
                        .uri("lb://FOODANDMART"))

                // 2. Protected FoodAndMart Route
                .route("foodandmart-protected", r -> r.path("/api/fm/**")
                        .filters(f -> {
                           log.info(" Applying AuthenticationFilter to /api/fm request...");
                            // This manually applies your custom filter
                            return f.filter(authFilter.apply(new AuthenticationFilter.Config()));
                        })
                        .uri("lb://FOODANDMART"))

                .route("customerandorder-docs", r -> r.path("/api/co/v3/api-docs")
                        // Notice: NO AuthenticationFilter applied here!
                        .uri("lb://CUSTOMERANDORDER"))

                // 3. Protected CustomerAndOrder Route
                .route("customerandorder-protected", r -> r.path("/api/co/**")
                .filters(f -> {
                    log.info("Applying AuthenticationFilter to /api/co request...");
                    // This manually applies your custom filter
                    return f.filter(authFilter.apply(new AuthenticationFilter.Config()));
                })
                .uri("lb://CUSTOMERANDORDER"))

                .route("driver-docs", r -> r.path("/api/driver/v3/api-docs")
                        // Notice: NO AuthenticationFilter applied here!
                        .uri("lb://DRIVER"))

                .route("driver-protected", r -> r.path("/api/driver/**")
                        .filters(f -> {
                            log.info("Applying AuthenticationFilter to /api/driver request...");
                            // This manually applies your custom filter
                            return f.filter(authFilter.apply(new AuthenticationFilter.Config()));
                        })
                        .uri("lb://DRIVER"))

                .route("division-docs", r -> r.path("/api/div/v3/api-docs")
                        // Notice: NO AuthenticationFilter applied here!
                        .uri("lb://DIVISION"))

                .route("division-protected", r -> r.path("/api/div/**")
                        .filters(f -> {
                            log.info("Applying AuthenticationFilter to /api/div request...");
                            // This manually applies your custom filter
                            return f.filter(authFilter.apply(new AuthenticationFilter.Config()));
                        })
                        .uri("lb://DIVISION"))

                .route("notification-docs", r -> r.path("/api/notification/v3/api-docs")
                        // Notice: NO AuthenticationFilter applied here!
                        .uri("lb://NOTIFICATION"))

                .route("notification-protected", r -> r.path("/api/notification/**")
                        .filters(f -> {
                            log.info("Applying AuthenticationFilter to /api/notification request...");
                            // This manually applies your custom filter
                            return f.filter(authFilter.apply(new AuthenticationFilter.Config()));
                        })
                        .uri("lb://NOTIFICATION"))
                .build();
    }*/

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, AuthenticationFilter authFilter) {
        return builder.routes()

                // 2. PUBLIC BYPASS ROUTE FOR FOODANDMART SWAGGER DOCS JSON
                .route("foodandmart-docs", r -> r.path("/api/fm/v3/api-docs")
                        .uri("lb://FOODANDMART"))

                // 1. Auth Service Route (Permit All)
                .route("auth-service", r -> r.path("/api/fm/auth/**")
                        .filters(f -> {
                            log.info("DEBUG: Routing public login/registration to Auth Service...");
                            // CRUCIAL: Strips /api/fm from the path so downstream microservice gets /auth/login
                            return f;
                        })
                        .uri("lb://FOODANDMART"))

                .route("foodandmart-login", r -> r.path("/api/fm/users/findByUserIdAndUserType")
                        .filters(f -> {
                            return f;
                        })
                        .uri("lb://FOODANDMART"))
                .route("co-foodandmart-login", r -> r.path("/api/fm/users/createUser")
                        .filters(f -> {
                            return f;
                        })
                        .uri("lb://FOODANDMART"))


                // 3. PROTECTED SERVICE ENDPOINTS (Applies your Auth Filter)
                .route("foodandmart-protected", r -> r.path("/api/fm/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://FOODANDMART"))

                // 1. Customer Service Route (Permit All)
                .route("co-auth-service", r -> r.path("/api/co/auth/**")
                        .filters(f -> {
                            log.info("DEBUG: Routing public login/registration to Auth Service...");
                            // CRUCIAL: Strips /api/fm from the path so downstream microservice gets /auth/login
                            return f;
                        })
                        .uri("lb://CUSTOMERANDORDER"))

                // 4. PUBLIC BYPASS ROUTE FOR CUSTOMERANDORDER SWAGGER DOCS JSON
                .route("customerandorder-docs", r -> r.path("/api/co/v3/api-docs")
                        .uri("lb://CUSTOMERANDORDER"))

                // 5. PROTECTED SERVICE ENDPOINTS (Applies your Auth Filter)
                .route("customerandorder-protected", r -> r.path("/api/co/**")
                        .filters(f -> f.filter(authFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://CUSTOMERANDORDER"))

                .route("driver-docs", r -> r.path("/api/driver/v3/api-docs")
                        // Notice: NO AuthenticationFilter applied here!
                        .uri("lb://DRIVER"))

                .route("driver-protected", r -> r.path("/api/driver/**")
                        .filters(f -> {
                            log.info("Applying AuthenticationFilter to /api/driver request...");
                            // This manually applies your custom filter
                            return f.filter(authFilter.apply(new AuthenticationFilter.Config()));
                        })
                        .uri("lb://DRIVER"))

                .route("division-docs", r -> r.path("/api/div/v3/api-docs")
                        // Notice: NO AuthenticationFilter applied here!
                        .uri("lb://DIVISION"))

                .route("division-protected", r -> r.path("/api/div/**")
                        .filters(f -> {
                            log.info("Applying AuthenticationFilter to /api/div request...");
                            // This manually applies your custom filter
                            return f.filter(authFilter.apply(new AuthenticationFilter.Config()));
                        })
                        .uri("lb://DIVISION"))

                .route("notification-docs", r -> r.path("/api/notification/v3/api-docs")
                        // Notice: NO AuthenticationFilter applied here!
                        .uri("lb://NOTIFICATION"))

                .route("notification-protected", r -> r.path("/api/notification/**")
                        .filters(f -> {
                            log.info("Applying AuthenticationFilter to /api/notification request...");
                            // This manually applies your custom filter
                            return f.filter(authFilter.apply(new AuthenticationFilter.Config()));
                        })
                        .uri("lb://NOTIFICATION"))

                .build();
    }
}



