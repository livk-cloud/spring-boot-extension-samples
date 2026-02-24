package com.livk.auth.server;

import com.livk.auth.server.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author livk
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

	private final OAuth2AuthorizationService authorizationService;

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

}
