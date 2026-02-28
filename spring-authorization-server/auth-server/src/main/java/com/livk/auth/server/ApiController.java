package com.livk.auth.server;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.livk.auth.server.common.util.SecurityUtils;
import com.livk.commons.web.HttpParameters;
import com.nimbusds.jose.util.Base64;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

/**
 * @author livk
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

	private final OAuth2AuthorizationService authorizationService;

	@Value("${server.port}")
	private String port;

	private final RestClient restClient = RestClient.create();

	@PostMapping("/login")
	public AccessToken login(@RequestParam String username, @RequestParam String password) {
		var params = new HttpParameters();
		params.set("grant_type", "password");
		params.set("username", username);
		params.set("password", password);
		params.set("scope", "livk.read");
		return restClient.post()
			.uri("http://localhost:" + port + "/oauth2/token")
			.header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encode("livk-client:secret"))
			.body(params)
			.retrieve()
			.body(AccessToken.class);
	}

	@GetMapping("/hello")
	public HttpEntity<String> hello() {
		log.info("hello user:{}", SecurityUtils.getUser());
		return ResponseEntity.ok("hello");
	}

	@PostMapping("/logout")
	public HttpEntity<String> logout(@RequestHeader("Authorization") String authorization) {
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		log.info("logout user:{}", SecurityUtils.getUser());
		String token = authorization.substring(7);
		// 找到 token 对应的授权信息
		OAuth2Authorization authorizationObj = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
		if (authorizationObj == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		authorizationService.remove(authorizationObj);
		return ResponseEntity.ok().build();
	}

	@Data
	public static class AccessToken {

		@JsonAlias("access_token")
		private String accessToken;

		@JsonAlias("refresh_token")
		private String refreshToken;

		@JsonAlias("expires_in")
		private Integer expiresIn;

	}

}
