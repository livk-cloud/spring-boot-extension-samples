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

import com.livk.commons.web.HttpParameters;
import com.nimbusds.jose.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author livk
 */
@SpringBootTest({ "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.url=jdbc:h2:mem:test",
		"spring.sql.init.schema-locations[0]=classpath*:/org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql",
		"spring.sql.init.schema-locations[1]=classpath*:/org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql",
		"spring.sql.init.schema-locations[2]=classpath*:/org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql",
		"spring.sql.init.platform=h2", "spring.sql.init.mode=embedded" })
@AutoConfigureMockMvc
class AuthServerAppTest {

	@Autowired
	MockMvcTester tester;

	@Test
	void testPassword() {
		var params = new HttpParameters();
		params.set("grant_type", "password");
		params.set("username", "livk");
		params.set("password", "123456");
		params.set("scope", "livk.read");
		JsonNode body = tester.post()
			.uri("/oauth2/token")
			.header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encode("livk-client:secret"))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.params(params)
			.assertThat()
			.debug()
			.hasStatusOk()
			.matches(jsonPath("sub").value("livk"))
			.matches(jsonPath("iss").value("http://localhost"))
			.matches(jsonPath("token_type").value("Bearer"))
			.matches(jsonPath("client_id").value("livk-client"))
			.matches(jsonPath("access_token").isNotEmpty())
			.matches(jsonPath("refresh_token").isNotEmpty())
			.bodyJson()
			.convertTo(JsonNode.class)
			.actual();

		String accessToken = body.get("access_token").asString();
		String refreshToken = body.get("refresh_token").asString();
		tester.get()
			.uri("/api/hello")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.assertThat()
			.debug()
			.hasStatusOk();

		var refreshTokenParams = new HttpParameters();
		refreshTokenParams.set("grant_type", "refresh_token");
		refreshTokenParams.set("refresh_token", refreshToken);

		body = tester.post()
			.uri("/oauth2/token")
			.header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encode("livk-client:secret"))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.params(refreshTokenParams)
			.assertThat()
			.debug()
			.hasStatusOk()
			.matches(jsonPath("scope").value("livk.read"))
			.matches(jsonPath("token_type").value("Bearer"))
			.matches(jsonPath("refresh_token").isNotEmpty())
			.matches(jsonPath("access_token").isNotEmpty())
			.bodyJson()
			.convertTo(JsonNode.class)
			.actual();

		accessToken = body.get("access_token").asString();
		tester.post()
			.uri("/api/logout")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.assertThat()
			.debug()
			.hasStatusOk();

		tester.get()
			.uri("/api/hello")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.assertThat()
			.debug()
			.hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void testSms() {
		var params = new HttpParameters();
		params.set("grant_type", "sms");
		params.set("mobile", "18664960000");
		params.set("code", "123456");
		params.set("scope", "livk.read");
		JsonNode body = tester.post()
			.uri("/oauth2/token")
			.header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encode("livk-client:secret"))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.params(params)
			.assertThat()
			.debug()
			.hasStatusOk()
			.matches(jsonPath("sub").value("18664960000"))
			.matches(jsonPath("iss").value("http://localhost"))
			.matches(jsonPath("token_type").value("Bearer"))
			.matches(jsonPath("client_id").value("livk-client"))
			.matches(jsonPath("access_token").isNotEmpty())
			.matches(jsonPath("refresh_token").isNotEmpty())
			.bodyJson()
			.convertTo(JsonNode.class)
			.actual();

		String accessToken = body.get("access_token").asString();
		tester.get()
			.uri("/api/hello")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.assertThat()
			.debug()
			.hasStatusOk()
			.bodyText()
			.isEqualTo("hello");

		tester.post()
			.uri("/api/logout")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.assertThat()
			.debug()
			.hasStatusOk();

		tester.get()
			.uri("/api/hello")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
			.assertThat()
			.debug()
			.hasStatus(HttpStatus.UNAUTHORIZED);
	}

}
