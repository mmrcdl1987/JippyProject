package com.jippy.customerandorder.config;

import com.jippy.customerandorder.constants.COConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint for clients to connect to WebSocket (with SockJS fallback)
        registry.addEndpoint(COConstants.WEB_SOCKET_END_POINT)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix for topics clients will SUBSCRIBE to
        registry.enableSimpleBroker(COConstants.WEB_SOCKET_TOPIC);

        // Prefix for messages sent FROM client TO server
        registry.setApplicationDestinationPrefixes("/app");
    }
}
