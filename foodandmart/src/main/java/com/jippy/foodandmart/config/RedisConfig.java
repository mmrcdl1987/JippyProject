package com.jippy.foodandmart.config;

import com.jippy.foodandmart.dto.AreaBannerCacheDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

//    @Bean
//    public StringRedisTemplate stringRedisTemplate(
//            RedisConnectionFactory connectionFactory) {
//
//        return new StringRedisTemplate(connectionFactory);
//    }

    @Bean
    public RedisTemplate<String, AreaBannerCacheDto> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, AreaBannerCacheDto> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(
                new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();

        return template;
    }
}