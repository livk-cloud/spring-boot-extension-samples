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

package com.livk.ck.jdbc.controller;

import com.livk.ck.jdbc.entity.User;
import com.livk.commons.jackson.JsonMapperUtils;
import com.livk.commons.util.Jsr310Utils;
import com.livk.testcontainers.DockerImageNames;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author livk
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true, parallel = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {

	@Container
	@ServiceConnection
	static final ClickHouseContainer clickhouse = new ClickHouseContainer(DockerImageNames.clickhouse())
		.withExposedPorts(8123, 9000);

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", () -> "jdbc:clickhouse://" + clickhouse.getHost() + ":"
				+ clickhouse.getFirstMappedPort() + "/" + clickhouse.getDatabaseName());
		registry.add("spring.datasource.username", clickhouse::getUsername);
		registry.add("spring.datasource.password", clickhouse::getPassword);
	}

	@Autowired
	MockMvcTester tester;

	@Order(2)
	@Test
	void testList() {
		tester.get()
			.uri("/user")
			.assertThat()
			.hasStatusOk()
			.matches(jsonPath("[0].appId").value("appId"))
			.matches(jsonPath("[0].version").value("version"));
	}

	@Order(3)
	@Test
	void testRemove() {
		var format = Jsr310Utils.formatDate(LocalDateTime.now());
		tester.delete().uri("/user/" + format).assertThat().hasStatusOk();

		tester.get().uri("/user").assertThat().hasStatusOk().matches(jsonPath("$", hasSize(0)));
	}

	@Order(1)
	@Test
	void testSave() {
		var user = new User().setId(Integer.MAX_VALUE)
			.setAppId("appId")
			.setVersion("version")
			.setRegTime(LocalDate.now());
		tester.post()
			.uri("/user")
			.contentType(MediaType.APPLICATION_JSON)
			.content(JsonMapperUtils.writeValueAsString(user))
			.assertThat()
			.hasStatusOk()
			.bodyText()
			.isEqualTo("true");
	}

}
