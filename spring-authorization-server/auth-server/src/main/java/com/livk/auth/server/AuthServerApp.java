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

package com.livk.auth.server;

import com.livk.commons.SpringContextHolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author livk
 */
@RestController
@SpringBootApplication
public class AuthServerApp {

	void main(String[] args) {
		SpringApplication.run(AuthServerApp.class, args);
	}

	@GetMapping("/api/hello")
	public HttpEntity<String> hello() {
		return ResponseEntity.ok("hello");
	}

	@PostMapping("/api/logout")
	public void logout(@RequestHeader("Authorization") String authorization) {
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return;
		}

		String token = authorization.substring(7);

		OAuth2AuthorizationService authorizationService = SpringContextHolder.getBean(OAuth2AuthorizationService.class);
		// 找到 token 对应的授权信息
		OAuth2Authorization authorizationObj = authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);

		if (authorizationObj != null) {
			authorizationService.remove(authorizationObj);
		}
	}

}
