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

package com.livk.auth.server.config;

import com.livk.auth.server.common.constant.SecurityConstants;
import com.livk.auth.server.common.converter.OAuth2PasswordAuthenticationConverter;
import com.livk.auth.server.common.converter.OAuth2SmsAuthenticationConverter;
import com.livk.auth.server.common.core.FormIdentityLoginConfigurer;
import com.livk.auth.server.common.core.UserDetailsAuthenticationProvider;
import com.livk.auth.server.common.core.customizer.OAuth2JwtTokenCustomizer;
import com.livk.auth.server.common.core.resource.BearerTokenExtractor;
import com.livk.auth.server.common.handler.AuthenticationFailureEventHandler;
import com.livk.auth.server.common.handler.AuthenticationSuccessEventHandler;
import com.livk.auth.server.common.provider.OAuth2PasswordAuthenticationProvider;
import com.livk.auth.server.common.provider.OAuth2SmsAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * @author livk
 */
@Configuration
public class AuthorizationServerConfiguration {

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
			OAuth2AuthorizationService authorizationService, OAuth2TokenGenerator<OAuth2Token> oAuth2TokenGenerator,
			UserDetailsAuthenticationProvider userDetailsAuthenticationProvider) throws Exception {
		var authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
		http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher());
		http.with(authorizationServerConfigurer, withDefaults());
		var authenticationManager = http.getSharedObject(AuthenticationManagerBuilder.class).build();
		var passwordAuthenticationProvider = new OAuth2PasswordAuthenticationProvider(authenticationManager,
				authorizationService, oAuth2TokenGenerator);
		var smsAuthenticationProvider = new OAuth2SmsAuthenticationProvider(authenticationManager, authorizationService,
				oAuth2TokenGenerator);
		http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
			.tokenEndpoint(
					(tokenEndpoint) -> tokenEndpoint.accessTokenRequestConverter(this.accessTokenRequestConverter())
						.authenticationProvider(passwordAuthenticationProvider)
						.authenticationProvider(smsAuthenticationProvider)
						.accessTokenResponseHandler(new AuthenticationSuccessEventHandler())
						.errorResponseHandler(new AuthenticationFailureEventHandler()))
			.authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
				.consentPage(SecurityConstants.CUSTOM_CONSENT_PAGE_URI))
			.authorizationService(authorizationService);
		http.with(new FormIdentityLoginConfigurer(), Customizer.withDefaults());
		http.authenticationManager(authenticationManager);
		http.securityContext(contextConfigurer -> contextConfigurer
			.securityContextRepository(new HttpSessionSecurityContextRepository()));
		http.csrf(csrfConfigurer -> csrfConfigurer
			.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()));
		http.authenticationProvider(userDetailsAuthenticationProvider);
		return http.build();
	}

	@Bean
	public OAuth2TokenGenerator<OAuth2Token> oAuth2TokenGenerator() {
		OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
		accessTokenGenerator.setAccessTokenCustomizer(new OAuth2JwtTokenCustomizer());
		return new DelegatingOAuth2TokenGenerator(accessTokenGenerator, new OAuth2RefreshTokenGenerator());
	}

	private AuthenticationConverter accessTokenRequestConverter() {
		return new DelegatingAuthenticationConverter(new OAuth2AuthorizationCodeAuthenticationConverter(),
				new OAuth2ClientCredentialsAuthenticationConverter(), new OAuth2RefreshTokenAuthenticationConverter(),
				new OAuth2PasswordAuthenticationConverter(), new OAuth2SmsAuthenticationConverter());
	}

	@Bean
	public SecurityFilterChain resourceServer(HttpSecurity http, BearerTokenExtractor tokenExtractor,
			OpaqueTokenIntrospector opaqueTokenIntrospector) throws Exception {
		http.securityMatcher(_ -> true)
			.authorizeHttpRequests(registry -> registry.requestMatchers("/auth/**", "/actuator/**", "/css/**", "/error")
				.permitAll()
				.anyRequest()
				.authenticated())
			.oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(token -> token.introspector(opaqueTokenIntrospector))
				.bearerTokenResolver(tokenExtractor))
			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
			.csrf(AbstractHttpConfigurer::disable);

		return http.build();
	}

}
