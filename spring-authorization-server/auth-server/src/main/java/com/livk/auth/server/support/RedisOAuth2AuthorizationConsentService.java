package com.livk.auth.server.support;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author livk
 */
@Component
public class RedisOAuth2AuthorizationConsentService implements OAuth2AuthorizationConsentService {

	private static final String CONSENT_KEY_PREFIX = "oauth2:consent:";

	private final RedisTemplate<String, Object> redisTemplate;

	public RedisOAuth2AuthorizationConsentService(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void save(OAuth2AuthorizationConsent consent) {
		Assert.notNull(consent, "consent cannot be null");

		String key = buildKey(consent.getRegisteredClientId(), consent.getPrincipalName());

		redisTemplate.opsForValue().set(key, consent);
	}

	@Override
	public void remove(OAuth2AuthorizationConsent consent) {
		Assert.notNull(consent, "consent cannot be null");

		String key = buildKey(consent.getRegisteredClientId(), consent.getPrincipalName());

		redisTemplate.delete(key);
	}

	@Override
	public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {

		Assert.hasText(registeredClientId, "registeredClientId cannot be empty");
		Assert.hasText(principalName, "principalName cannot be empty");

		String key = buildKey(registeredClientId, principalName);
		return (OAuth2AuthorizationConsent) redisTemplate.opsForValue().get(key);
	}

	/* ---------------- internal ---------------- */

	private String buildKey(String clientId, String principalName) {
		return CONSENT_KEY_PREFIX + clientId + ":" + principalName;
	}

}
