package com.jippy.customerandorder.config;

import com.jippy.customerandorder.constants.COConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private StompUserInterceptor stompUserInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // 1. Raw WebSocket endpoint (for Postman / Native WS Clients)
        registry.addEndpoint(COConstants.WEB_SOCKET_END_POINT)
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix for topics clients will SUBSCRIBE to
        registry.enableSimpleBroker(COConstants.WEB_SOCKET_TOPIC);

        // Prefix for messages sent FROM client TO server
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        registration.interceptors(stompUserInterceptor);
    }
}
