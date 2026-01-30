package com.livk.admin.server.config;

import com.livk.commons.jackson.TypeFactoryUtils;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * <p>
 * AdminRedisConfig
 * </p>
 *
 * @author livk
 * @date 2026/1/27
 */
@Configuration
public class AdminRedisConfig {

	@Bean
	public ReactiveRedisTemplate<String, Object> reactiveRedisOps(SimpleModule adminJacksonModule,
			ReactiveRedisConnectionFactory redisConnectionFactory) {
		var mapper = JsonMapper.builder().addModule(adminJacksonModule).build();
		var hashKeySerializer = new JacksonJsonRedisSerializer<>(mapper, InstanceId.class);
		var collectionType = TypeFactoryUtils.listType(InstanceEvent.class);
		var hashValueSerializer = new JacksonJsonRedisSerializer<>(mapper, collectionType);
		var serializationContext = RedisSerializationContext.<String, Object>newSerializationContext()
			.key(RedisSerializer.string())
			.value(new JacksonJsonRedisSerializer<>(mapper, Object.class))
			.hashKey(hashKeySerializer)
			.hashValue(hashValueSerializer)
			.build();
		return new ReactiveRedisTemplate<>(redisConnectionFactory, serializationContext);
	}

}
