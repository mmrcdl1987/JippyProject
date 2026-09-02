package com.jippy.gatewayserver.config;


import com.jippy.gatewayserver.securty.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtils jwtUtils;

    private static final List<String> EXCLUDED_URLS = List.of(
            "/v3/api-docs",
            "/swagger-ui",
            "/webjars",
            "/swagger-ui.html",
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
//            From DIV
            "/api/div/email/sendOtp"


    );

    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {
        // You can leave this empty for now
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {
            log.info("AuthenticationFilter triggered for: {}", exchange.getRequest().getPath());
            ServerHttpRequest request = exchange.getRequest();

            // For react UI -- gateway  CRITICAL CORS FIX: Pass through all HTTP OPTIONS (Preflight) requests safely
            if (org.springframework.http.HttpMethod.OPTIONS.equals(request.getMethod())) {
                return chain.filter(exchange);
            }

            // 2. CRITICAL WEBSOCKET FIX: Skip HTTP token check for WebSocket Handshake upgrades
            String upgradeHeader = request.getHeaders().getFirst(HttpHeaders.UPGRADE);
            if ("websocket".equalsIgnoreCase(upgradeHeader)) {
                log.info("GATEWAY: WebSocket Upgrade request detected for path: {}. Bypassing HTTP JWT validation.", request.getPath());
                return chain.filter(exchange);
            }

            String path = exchange.getRequest().getURI().getPath();
            boolean isExcluded = EXCLUDED_URLS.stream().anyMatch(path::contains);

            if (isExcluded) {
                return chain.filter(exchange); // This passes the request directly to Swagger UI without hitting the token check!
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Token");
            }

            String authHeader = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);
            log.info("GATEWAY: Received token: {}", authHeader);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            }

            try {
                // Validate signature and expiry
                if (!jwtUtils.validateToken(authHeader)) {
                    throw new RuntimeException("Invalid Token");
                }

                String username = jwtUtils.getUsernameFromToken(authHeader);
                List<String> roles = jwtUtils.getRolesFromToken(authHeader);

                // IMPORTANT: You must return the filtered chain with the MUTATED exchange
                ServerHttpRequest modifiedRequest = request.mutate()
                        .headers(httpHeaders -> {
                            httpHeaders.remove("X-Auth-User");
                            httpHeaders.remove("X-Auth-Roles");
                        })
                        .header("X-Auth-User", username)
                        .header("X-Auth-Roles", String.join(",", roles))
                        .build();

                log.info("GATEWAY: Adding header for user: {}", username);
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
            }
        };
    }
}
