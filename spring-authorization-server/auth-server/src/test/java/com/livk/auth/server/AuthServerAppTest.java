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

import com.livk.commons.jackson.JsonMapperUtils;
import com.nimbusds.jose.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
	MockMvc mockMvc;

	@Test
	void testPassword() throws Exception {
		var params = new LinkedMultiValueMap<String, String>();
		params.set("grant_type", "password");
		params.set("username", "livk");
		params.set("password", "123456");
		params.set("scope", "livk.read");
		byte[] body = mockMvc
			.perform(post("/oauth2/token")
				.header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encode("livk-client:secret"))
				.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
				.params(params))
			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(jsonPath("sub").value("livk"))
			.andExpect(jsonPath("iss").value("http://localhost"))
			.andExpect(jsonPath("token_type").value("Bearer"))
			.andExpect(jsonPath("client_id").value("livk-client"))
			.andExpect(jsonPath("access_token").isNotEmpty())
			.andReturn()
			.getResponse()
			.getContentAsByteArray();

		String accessToken = JsonMapperUtils.readTree(body).get("access_token").asText();
		mockMvc.perform(get("/api/hello").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(content().string("hello"));

		mockMvc.perform(post("/api/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andDo(print());

		mockMvc.perform(get("/api/hello").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()))
			.andDo(print());
	}

	@Test
	void testSms() throws Exception {
		var params = new LinkedMultiValueMap<String, String>();
		params.set("grant_type", "sms");
		params.set("mobile", "18664960000");
		params.set("code", "123456");
		params.set("scope", "livk.read");
		byte[] body = mockMvc
			.perform(post("/oauth2/token")
				.header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encode("livk-client:secret"))
				.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
				.params(params))
			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(jsonPath("sub").value("livk"))
			.andExpect(jsonPath("iss").value("http://localhost"))
			.andExpect(jsonPath("token_type").value("Bearer"))
			.andExpect(jsonPath("client_id").value("livk-client"))
			.andExpect(jsonPath("access_token").isNotEmpty())
			.andReturn()
			.getResponse()
			.getContentAsByteArray();

		String accessToken = JsonMapperUtils.readTree(body).get("access_token").asText();
		mockMvc.perform(get("/api/hello").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(content().string("hello"));

		mockMvc.perform(post("/api/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andDo(print());

		mockMvc.perform(get("/api/hello").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()))
			.andDo(print());
	}

}
