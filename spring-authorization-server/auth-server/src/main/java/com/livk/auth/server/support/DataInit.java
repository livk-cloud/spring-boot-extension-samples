package com.livk.auth.server.support;

import com.livk.auth.server.common.constant.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * <p>
 * DataInit
 * </p>
 *
 * @author livk
 * @date 2026/11/21
 */
@Component
@RequiredArgsConstructor
public class DataInit implements InitializingBean {

	private final RegisteredClientRepository registeredClientRepository;

	private final PasswordEncoder passwordEncoder;

	@Override
	public void afterPropertiesSet() {
		var client = RegisteredClient.withId("livk")
			.clientId("livk-client")
			.clientSecret(passwordEncoder.encode("secret"))
			.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
			.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
			.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_JWT)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
			.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
			.authorizationGrantType(SecurityConstants.GRANT_TYPE_PASSWORD)
			.authorizationGrantType(SecurityConstants.GRANT_TYPE_SMS)
			.redirectUri("http://127.0.0.1:8080/login/oauth2/code/livk-client-oidc")
			.redirectUri("http://127.0.0.1:8080/authorized")
			.redirectUri("https://www.baidu.com")
			.scope(OidcScopes.OPENID)
			.scope("livk.read")
			.scope("livk.write")
			.tokenSettings(TokenSettings.builder()
				.accessTokenFormat(OAuth2TokenFormat.REFERENCE)
				.accessTokenTimeToLive(Duration.ofHours(24L))
				.refreshTokenTimeToLive(Duration.ofHours(12L))
				.reuseRefreshTokens(false)
				.build())
			.clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
			.build();
		registeredClientRepository.save(client);
	}

}
