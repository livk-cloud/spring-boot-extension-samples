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

package com.livk.sso.resource.controller;

import com.livk.commons.http.annotation.EnableHttpClient;
import com.livk.commons.http.annotation.HttpClientType;
import com.livk.commons.jackson.JsonMapperUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.client.RestClient;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author livk
 */
@Disabled("需要启动授权服务器")
@EnableHttpClient(HttpClientType.REST_CLIENT)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

	@Autowired
	MockMvcTester tester;

	@Autowired
	RestClient restClient;

	String token;

	@BeforeEach
	public void init() {
		var body = new HashMap<>();
		body.put("username", "livk");
		body.put("password", "123456");
		var responseEntity = restClient.post()
			.uri("http://localhost:9987/login")
			.body(body)
			.retrieve()
			.toEntity(String.class);
		assertThat(responseEntity.getBody()).isNotBlank();
		token = "Bearer "
				+ JsonMapperUtils.readValueMap(responseEntity.getBody(), String.class, String.class).get("data");
	}

	@Test
	void test() {
		System.out.println(token);
	}

	@Test
	void testList() {
		tester.get()
			.uri("/user/list")
			.header(HttpHeaders.AUTHORIZATION, token)
			.assertThat()
			.hasStatusOk()
			.bodyText()
			.isEqualTo("list");
	}

	@Test
	void testUpdate() {
		tester.put()
			.uri("/user/update")
			.header(HttpHeaders.AUTHORIZATION, token)
			.assertThat()
			.hasStatusOk()
			.bodyText()
			.isEqualTo("update");
	}

}
