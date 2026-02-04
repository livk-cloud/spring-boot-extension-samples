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

package com.livk.rest;

import com.livk.commons.jackson.JsonMapperUtils;
import com.livk.rest.entity.User;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author livk
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureMockMvc
class RestAppTest {

	@Autowired
	MockMvcTester tester;

	@Order(2)
	@Test
	void testGetById() {
		tester.get()
			.uri("/rest/api/user/{id}", 1)
			.assertThat()
			.hasStatusOk()
			.matches(jsonPath("username").value("root"))
			.matches(jsonPath("password").value("root"))
			.matches(jsonPath("age").value(18));
	}

	@Order(1)
	@Test
	void testSave() {
		var user = new User();
		user.setUsername("root");
		user.setPassword("root");
		user.setAge(18);
		tester.post()
			.uri("/rest/api/user")
			.contentType(MediaType.APPLICATION_JSON)
			.content(JsonMapperUtils.writeValueAsString(user))
			.assertThat()
			.hasStatus(HttpStatus.CREATED);
	}

	@Order(3)
	@Test
	void testUpdate() {
		var user = new User();
		user.setUsername("admin");
		user.setPassword("admin");
		user.setAge(19);
		tester.put()
			.uri("/rest/api/user/{id}", 1)
			.contentType(MediaType.APPLICATION_JSON)
			.content(JsonMapperUtils.writeValueAsString(user))
			.assertThat()
			.hasStatus(HttpStatus.NO_CONTENT);
	}

	@Order(6)
	@Test
	void testDelete() {
		tester.delete().uri("/rest/api/user/{id}", 1).assertThat().hasStatus(HttpStatus.NO_CONTENT);
	}

	@Order(4)
	@Test
	void testAuth() {
		tester.get()
			.uri("/rest/api/user/search/auth")
			.param("name", "admin")
			.param("pwd", "admin")
			.assertThat()
			.hasStatusOk()
			.matches(jsonPath("$.username").value("admin"))
			.matches(jsonPath("$.password").value("admin"))
			.matches(jsonPath("$.age").value(19));
	}

	@Order(5)
	@Test
	void testList() {
		tester.get()
			.uri("/rest/api/user")
			.assertThat()
			.hasStatusOk()
			.matches(jsonPath("_embedded.users[0].username").value("admin"))
			.matches(jsonPath("_embedded.users[0].password").value("admin"))
			.matches(jsonPath("_embedded.users[0].age").value(19))
			.matches(jsonPath("page.size").value(20))
			.matches(jsonPath("page.totalElements").value(1))
			.matches(jsonPath("page.totalPages").value(1))
			.matches(jsonPath("page.number").value(0));
	}

}
