package com.livk.auth.server.config;

import com.livk.auth.server.common.resource.BearerTokenExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;

/**
 * <p>
 * ResourceServerConfig
 * </p>
 *
 * @author livk
 * @date 2025/11/27
 */
@Configuration
public class ResourceServerConfig {

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
