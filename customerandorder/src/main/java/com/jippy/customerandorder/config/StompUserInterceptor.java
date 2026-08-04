package com.jippy.customerandorder.config;

import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StompUserInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Test log to see EVERY frame
        log.info("Received STOMP Command: {}", accessor != null ? accessor.getCommand() : "NULL");

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // 1. Get raw Authorization header string
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && !token.isBlank()) {

                // If token happens to have 'Bearer ', strip it, otherwise use raw string directly
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }

                try {
                    // 2. Validate Raw Token
                    if (jwtUtils.validateToken(token)) {
                        String username = jwtUtils.getUsernameFromToken(token);
                        List<String> roles = jwtUtils.getRolesFromToken(token);

                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                        log.info("Authenticated STOMP User: " + username + ", Roles: " + roles);

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);

                        // 3. Attach User Principal to STOMP Session Context
                        accessor.setUser(auth);
                        log.info("STOMP Session Authenticated for User: {}, Roles: {}", username, roles);
                    }
                } catch (Exception e) {
                    log.error("Failed to authenticate STOMP connection: {}", e.getMessage());
                }
            } else {
                log.warn("No Authorization header found in STOMP CONNECT frame");
            }
        }
        return message;
    }
}
