package com.jippy.gatewayserver.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2) // Use -2 to run even before your AuthenticationFilter
public class FirstStepFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("1. GATEWAY RECEIVED REQUEST: " + exchange.getRequest().getPath());

        /*SecretKey key = Jwts.SIG.HS256.key().build();
        String encodedKey = Encoders.BASE64.encode(key.getEncoded());
        System.out.println(encodedKey);*/
        return chain.filter(exchange);
    }
}
