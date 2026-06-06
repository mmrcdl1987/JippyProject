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
            "/swagger-ui.html"
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
