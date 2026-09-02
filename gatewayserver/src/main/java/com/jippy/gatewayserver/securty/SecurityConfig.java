package com.jippy.gatewayserver.securty;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
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
                        .pathMatchers("/ws-group-order",
                                "/ws-group-order/**",
                                // State,city,area - APIs
                                "/api/fm/location/fetchStates",
                                "/api/fm/location/fetchCityInState",
                                "/api/fm/location/fetchAreaInCity",
                                // Address
                                "/api/fm/outlets/saveAddressDetails",
                                // User Creation
                                "/api/fm/users/createUser",
                                // Driver Creation
                                "/api/driver/postDriverDetails",
                                // Merchant Creation
                                "/api/fm/merchants/createMerchant",
                                // Requesting Approval while creation of[OUTLET,MERCHANT,DRIVER]
                                "/api/fm/approval-requests/createApprovalRequest",
                                // OTP APIs
                                "/api/fm/otp/send-signup-otp",
                                "/api/fm/otp/verify-signup-otp",
                                "/api/fm/otp/send-create-outlet-otp",
                                "/api/fm/otp/verify-create-outlet-otp",
                                "/api/fm/otp/resend-signup-otp",
                                "/api/fm/otp/resend-create-outlet-otp",

                                // Forgot Password APIs
                                "/api/fm/forgetPasswordForUserTypeBySendingOtpToMail",
                                "/api/fm/validateForgotPasswordOTP",
                                "/api/fm/updateForgotPassword",

                                // Find By Email From Driver
                                "/api/driver/findByEmail",
                                // From DIV
                                "/api/div/email/sendOtp"
                        ).permitAll()
                        .anyExchange().permitAll() // Allow EVERYTHING temporarily
                )
                .build();
    }
}
