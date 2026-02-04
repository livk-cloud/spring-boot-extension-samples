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

package com.livk.sso.auth.controller;

import com.livk.sso.commons.entity.User;
import com.livk.sso.commons.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * @author livk
 */
@SpringBootTest({ "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.url=jdbc:h2:mem:test",
		"spring.sql.init.platform=h2", "spring.sql.init.mode=embedded" })
@AutoConfigureMockMvc
class UserControllerTest {

	@Autowired
	MockMvcTester tester;

	@Test
	void testList() {
		var user = new User().setUsername("livk").setPassword("123456");
		tester.get()
			.uri("/user/list")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtUtils.generateToken(user))
			.assertThat()
			.hasStatusOk()
			.bodyText()
			.matches("list");
	}

	@Test
	void testUpdate() {
		var user = new User().setUsername("livk").setPassword("123456");
		tester.put()
			.uri("/user/update")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtUtils.generateToken(user))
			.assertThat()
			.hasStatusOk()
			.bodyText()
			.isEqualTo("update");
	}

}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
