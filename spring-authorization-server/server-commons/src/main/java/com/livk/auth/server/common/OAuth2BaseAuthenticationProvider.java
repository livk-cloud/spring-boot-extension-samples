/*
 * Copyright 2021-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.livk.auth.server.common;

import com.google.common.collect.Sets;
import com.livk.auth.server.common.exception.BadCaptchaException;
import com.livk.auth.server.common.util.MessageSourceUtils;
import com.livk.auth.server.common.constant.OAuth2ErrorCodesExpand;
import com.livk.commons.util.ClassUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.security.Principal;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * 处理自定义授权
 * </p>
 *
 * @author livk
 */
@Slf4j
public abstract class OAuth2BaseAuthenticationProvider<T extends OAuth2BaseAuthenticationToken>
		implements AuthenticationProvider {

	protected static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1";

	protected final OAuth2AuthorizationService authorizationService;

	protected final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

	protected final AuthenticationManager authenticationManager;

	protected final MessageSourceAccessor messages;

	/**
	 * Constructs an {@code OAuth2AuthorizationCodeAuthenticationProvider} using the
	 * provided parameters.
	 * @param authorizationService the authorization service
	 * @param tokenGenerator the token generator
	 * @since 0.2.3
	 */
	protected OAuth2BaseAuthenticationProvider(AuthenticationManager authenticationManager,
			OAuth2AuthorizationService authorizationService,
			OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
		Assert.notNull(authorizationService, "authorizationService cannot be null");
		Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
		this.authenticationManager = authenticationManager;
		this.authorizationService = authorizationService;
		this.tokenGenerator = tokenGenerator;

		// 国际化配置
		this.messages = new MessageSourceAccessor(MessageSourceUtils.get(), Locale.CHINA);
	}

	/**
	 * 封装简易principal
	 */
	protected abstract UsernamePasswordAuthenticationToken assemble(Map<String, Object> reqParameters);

	/**
	 * 当前provider是否支持此令牌类型
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		var childType = ClassUtils.resolveTypeArgument(this.getClass(), OAuth2BaseAuthenticationProvider.class);
		Assert.notNull(childType, "child Type is null");
		return childType.isAssignableFrom(authentication);
	}

	/**
	 * 当前的请求客户端是否支持此模式
	 */
	protected abstract void checkClient(RegisteredClient registeredClient);

	/**
	 * Performs authentication with the same contract as
	 * @param authentication the authentication request object.
	 * @throws AuthenticationException if authentication fails.
	 * @see org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeAuthenticationProvider#authenticate(Authentication)
	 * @see org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationProvider#authenticate(Authentication)
	 * {@link AuthenticationManager#authenticate(Authentication)} .
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		@SuppressWarnings("unchecked")
		var baseAuthentication = (T) authentication;

		var clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(baseAuthentication);

		var registeredClient = clientPrincipal.getRegisteredClient();

		checkClient(registeredClient);

		var authorizedScopes = Objects.requireNonNull(registeredClient).getScopes();
		if (!CollectionUtils.isEmpty(baseAuthentication.getScopes())) {
			if (baseAuthentication.getScopes()
				.stream()
				.noneMatch(scope -> registeredClient.getScopes().contains(scope))) {
				throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE);
			}
			authorizedScopes = Sets.newLinkedHashSet(baseAuthentication.getScopes());
		}

		var reqParameters = baseAuthentication.getAdditionalParameters();
		try {
			var usernamePasswordAuthenticationToken = assemble(reqParameters);

			var principal = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

			var tokenContextBuilder = DefaultOAuth2TokenContext.builder()
				.registeredClient(registeredClient)
				.principal(principal)
				.authorizationServerContext(AuthorizationServerContextHolder.getContext())
				.authorizedScopes(authorizedScopes)
				.authorizationGrantType(baseAuthentication.getGrantType())
				.authorizationGrant(baseAuthentication);

			var authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
				.principalName(principal.getName())
				.authorizationGrantType(baseAuthentication.getGrantType())
				.authorizedScopes(authorizedScopes);

			var tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();

			var generatedAccessToken = Optional.ofNullable(this.tokenGenerator.generate(tokenContext))
				.orElseThrow(() -> new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
						"The token generator failed to generate the access token.", ERROR_URI)));

			var accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
					generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
					generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());

			if (generatedAccessToken instanceof ClaimAccessor) {
				authorizationBuilder
					.token(accessToken,
							metadata -> metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME,
									((ClaimAccessor) generatedAccessToken).getClaims()))
					.attribute(Principal.class.getName(), principal);
			}
			else {
				authorizationBuilder.accessToken(accessToken);
			}

			OAuth2RefreshToken refreshToken = null;
			if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)
					&& !clientPrincipal.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE)) {
				tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
				var generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
				if (generatedRefreshToken == null) {
					throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
							"The token generator failed to generate the refresh token.", ERROR_URI));
				}
				if (generatedRefreshToken instanceof OAuth2RefreshToken oAuth2RefreshToken) {
					refreshToken = oAuth2RefreshToken;
				}
				else {
					refreshToken = new OAuth2RefreshToken(generatedRefreshToken.getTokenValue(),
							generatedRefreshToken.getIssuedAt(), generatedRefreshToken.getExpiresAt());
				}
				authorizationBuilder.refreshToken(refreshToken);
			}

			var authorization = authorizationBuilder.build();

			this.authorizationService.save(authorization);

			log.debug("returning OAuth2AccessTokenAuthenticationToken");

			return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken,
					refreshToken, Objects.requireNonNull(authorization.getAccessToken().getClaims()));

		}
		catch (Exception ex) {
			log.error("problem in authenticate", ex);
			throw oAuth2AuthenticationException(authentication, (AuthenticationException) ex);
		}

	}

	protected OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(
			Authentication authentication) {
		OAuth2ClientAuthenticationToken clientPrincipal = null;
		if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
			clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
		}
		if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
			return clientPrincipal;
		}
		throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
	}

	/**
	 * 登录异常转换为OAuth2AuthenticationException异常，才能被AuthenticationFailureEventHandler处理
	 * @param authentication 身份验证
	 * @param authenticationException 身份验证异常
	 * @return {@link OAuth2AuthenticationException}
	 */
	protected OAuth2AuthenticationException oAuth2AuthenticationException(Authentication authentication,
			AuthenticationException authenticationException) {
		if (authenticationException instanceof UsernameNotFoundException) {
			return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodesExpand.USERNAME_NOT_FOUND,
					this.messages.getMessage("JdbcDaoImpl.notFound", new Object[] { authentication.getName() },
							"Username {0} not found"),
					""));
		}
		if (authenticationException instanceof BadCaptchaException) {
			return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodesExpand.BAD_CAPTCHA,
					this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCaptcha", "Bad captcha"),
					""));
		}
		if (authenticationException instanceof BadCredentialsException) {
			return new OAuth2AuthenticationException(
					new OAuth2Error(OAuth2ErrorCodesExpand.BAD_CREDENTIALS, this.messages.getMessage(
							"AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"), ""));
		}
		if (authenticationException instanceof LockedException) {
			return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodesExpand.USER_LOCKED, this.messages
				.getMessage("AbstractUserDetailsAuthenticationProvider.locked", "User account is locked"), ""));
		}
		if (authenticationException instanceof DisabledException) {
			return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodesExpand.USER_DISABLE,
					this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "User is disabled"),
					""));
		}
		if (authenticationException instanceof AccountExpiredException) {
			return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodesExpand.USER_EXPIRED, this.messages
				.getMessage("AbstractUserDetailsAuthenticationProvider.expired", "User account has expired"), ""));
		}
		if (authenticationException instanceof CredentialsExpiredException) {
			return new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodesExpand.CREDENTIALS_EXPIRED,
					this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.credentialsExpired",
							"User credentials have expired"),
					""));
		}
		return new OAuth2AuthenticationException(OAuth2ErrorCodesExpand.UN_KNOW_LOGIN_ERROR);
	}

}
