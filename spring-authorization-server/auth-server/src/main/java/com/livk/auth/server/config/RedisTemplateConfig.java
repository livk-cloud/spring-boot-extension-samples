package com.livk.auth.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * <p>
 * RedisTemplateConfig
 * </p>
 *
 * @author livk
 * @date 2026/11/21
 */
@Configuration
public class RedisTemplateConfig {

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		var redisTemplate = new RedisTemplate<String, Object>();
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setHashKeySerializer(RedisSerializer.string());
		redisTemplate.setConnectionFactory(connectionFactory);
		return redisTemplate;
	}

}
