package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    // Redis 默认使用JDK序列化方式
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 实现 JSON 的序列化方式，支持所有类。
        /**
         * {
         *   "@class": "com.artisan.domain.Artisan",
         *   "id": 100,
         *   "name": "小工匠",
         *   "sex": "Male"
         * }
         */

        // 设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());

        // 设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());

        // 设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());

        // 设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();
        return template;
    }
}
