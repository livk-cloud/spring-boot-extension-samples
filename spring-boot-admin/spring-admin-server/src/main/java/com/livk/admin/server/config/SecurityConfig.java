package com.livk.admin.server.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.util.UUID;

/**
 * <p>
 * SecurityConfig
 * </p>
 *
 * @author livk
 * @date 2026/1/27
 */
@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AdminServerProperties adminServer) {
		var successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
		successHandler.setTargetUrlParameter("redirectTo");
		successHandler.setDefaultTargetUrl(adminServer.path("/"));
		return http
			.authorizeHttpRequests(registry -> registry.requestMatchers(adminServer.path("/assets/**"))
				.permitAll()
				.requestMatchers(adminServer.path("/variables.css"))
				.permitAll()
				.requestMatchers(adminServer.path("/actuator/info"))
				.permitAll()
				.requestMatchers(adminServer.path("/actuator/health"))
				.permitAll()
				.requestMatchers(adminServer.path("/login"))
				.permitAll()
				.dispatcherTypeMatchers(DispatcherType.ASYNC)
				.permitAll()
				.anyRequest()
				.authenticated())
			.formLogin(configurer -> configurer.loginPage(adminServer.path("/login")).successHandler(successHandler))
			.httpBasic(Customizer.withDefaults())
			.csrf(configurer -> configurer.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.ignoringRequestMatchers(
						PathPatternRequestMatcher.withDefaults()
							.matcher(HttpMethod.POST, adminServer.path("/instances")),
						PathPatternRequestMatcher.withDefaults()
							.matcher(HttpMethod.DELETE, adminServer.path("/instances/**")),
						PathPatternRequestMatcher.withDefaults().matcher(adminServer.path("/actuator/**"))))
			.rememberMe(configurer -> configurer.key(UUID.randomUUID().toString()).tokenValiditySeconds(1209600))
			.build();
	}

}
