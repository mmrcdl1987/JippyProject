package com.jippy.customerandorder.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CoRedisConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisGroupExpiryListener expiryListener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 1. Programmatically enable Expiration notifications (Ex) in Redis
        try {
            connectionFactory.getConnection().serverCommands().setConfig("notify-keyspace-events", "Ex");
        } catch (Exception e) {
            // Log fallback warning if your Redis environment blocks runtime config changes
            log.error("Warning: Could not set Redis config programmatically. Ensure notify-keyspace-events 'Ex' is set in redis.conf");
        }

        // 2. Listen strictly to the Redis system topic for expired keys
        container.addMessageListener(expiryListener, new PatternTopic("__keyevent@0__:expired"));

        return container;
    }
}
