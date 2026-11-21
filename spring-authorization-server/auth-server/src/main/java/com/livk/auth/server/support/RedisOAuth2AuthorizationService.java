package com.livk.auth.server.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author livk
 */
@Slf4j
@Component
public class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

	private static final String AUTH_ID_KEY = "oauth2:auth:id:";

	private static final String TOKEN_KEY = "oauth2:auth:token:";

	private final RedisTemplate<String, Object> redisTemplate;

	public RedisOAuth2AuthorizationService(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void save(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");

		String authIdKey = AUTH_ID_KEY + authorization.getId();

		Duration ttl = resolveAuthorizationTtl(authorization);
		if (ttl.isNegative() || ttl.isZero()) {
			return;
		}
		redisTemplate.opsForValue().set(authIdKey, authorization, ttl);
		saveTokenIndex(authorization.getAccessToken(), authorization.getId());
		saveTokenIndex(authorization.getRefreshToken(), authorization.getId());
	}

	@Override
	public void remove(OAuth2Authorization authorization) {
		Assert.notNull(authorization, "authorization cannot be null");

		redisTemplate.delete(AUTH_ID_KEY + authorization.getId());

		deleteTokenIndex(authorization.getAccessToken());
		deleteTokenIndex(authorization.getRefreshToken());
	}

	@Override
	public OAuth2Authorization findById(String id) {
		Assert.hasText(id, "id cannot be empty");
		return (OAuth2Authorization) redisTemplate.opsForValue().get(AUTH_ID_KEY + id);
	}

	@Override
	public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
		Assert.hasText(token, "token cannot be empty");

		String tokenKey = TOKEN_KEY + token;
		Object authId = redisTemplate.opsForValue().get(tokenKey);

		if (authId == null) {
			return null;
		}

		return findById(authId.toString());
	}

	private void saveTokenIndex(OAuth2Authorization.Token<?> token, String OAuth2AuthorizationId) {
		if (token == null || token.getToken() == null) {
			log.error("saveTokenIndex failed, token is null");
			return;
		}

		Instant expiresAt = token.getToken().getExpiresAt();
		if (expiresAt == null) {
			return;
		}

		Duration ttl = Duration.between(Instant.now(), expiresAt);
		if (ttl.isNegative() || ttl.isZero()) {
			return;
		}

		String tokenValue = token.getToken().getTokenValue();
		String tokenKey = TOKEN_KEY + tokenValue;

		redisTemplate.opsForValue().set(tokenKey, OAuth2AuthorizationId, ttl);
	}

	private void deleteTokenIndex(OAuth2Authorization.Token<?> token) {
		if (token == null) {
			log.error("deleteTokenIndex failed, token is null");
			return;
		}
		String tokenValue = token.getToken().getTokenValue();
		redisTemplate.delete(TOKEN_KEY + tokenValue);
	}

	private Duration resolveAuthorizationTtl(OAuth2Authorization authorization) {
		List<OAuth2Authorization.Token<?>> tokens = new ArrayList<>();
		if (authorization.getAccessToken() != null) {
			tokens.add(authorization.getAccessToken());
		}
		if (authorization.getRefreshToken() != null) {
			tokens.add(authorization.getRefreshToken());
		}
		Instant maxExpire = null;

		for (OAuth2Authorization.Token<?> token : tokens) {
			Instant expiresAt = token.getToken().getExpiresAt();
			if (expiresAt != null) {
				maxExpire = (maxExpire == null || expiresAt.isAfter(maxExpire)) ? expiresAt : maxExpire;
			}
		}

		if (maxExpire == null) {
			return Duration.ZERO;
		}

		return Duration.between(Instant.now(), maxExpire);
	}

}
