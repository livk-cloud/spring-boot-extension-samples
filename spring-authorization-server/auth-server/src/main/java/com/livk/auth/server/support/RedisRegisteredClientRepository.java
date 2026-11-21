package com.livk.auth.server.support;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author livk
 */
@Component
public class RedisRegisteredClientRepository implements RegisteredClientRepository {

	private static final String ID_KEY_PREFIX = "oauth2:client:id:";

	private static final String CLIENT_ID_KEY_PREFIX = "oauth2:client:clientId:";

	private final RedisTemplate<String, Object> redisTemplate;

	public RedisRegisteredClientRepository(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void save(RegisteredClient registeredClient) {
		Assert.notNull(registeredClient, "registeredClient cannot be null");

		String idKey = ID_KEY_PREFIX + registeredClient.getId();
		String clientIdKey = CLIENT_ID_KEY_PREFIX + registeredClient.getClientId();

		redisTemplate.opsForValue().set(idKey, registeredClient);
		redisTemplate.opsForValue().set(clientIdKey, registeredClient);
	}

	@Override
	public RegisteredClient findById(String id) {
		Assert.hasText(id, "id cannot be empty");

		String idKey = ID_KEY_PREFIX + id;

		return (RegisteredClient) redisTemplate.opsForValue().get(idKey);
	}

	@Override
	public RegisteredClient findByClientId(String clientId) {
		Assert.hasText(clientId, "clientId cannot be empty");

		String clientIdKey = CLIENT_ID_KEY_PREFIX + clientId;
		return (RegisteredClient) redisTemplate.opsForValue().get(clientIdKey);
	}

}
