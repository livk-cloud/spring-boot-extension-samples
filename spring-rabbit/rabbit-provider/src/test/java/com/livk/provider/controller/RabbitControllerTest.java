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

package com.livk.provider.controller;

import com.livk.commons.jackson.JsonMapperUtils;
import com.livk.testcontainers.DockerImageNames;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.time.Duration;
import java.util.Map;

/**
 * @author livk
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true, parallel = true)
class RabbitControllerTest {

	@Container
	@ServiceConnection
	static final RabbitMQContainer rabbit = new RabbitMQContainer(DockerImageNames.rabbitmq())
		.withStartupTimeout(Duration.ofMinutes(4));

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("spring.rabbitmq.host", rabbit::getHost);
		registry.add("spring.rabbitmq.port", rabbit::getFirstMappedPort);
	}

	static final String body = JsonMapperUtils.writeValueAsString(Map.of("msg", "hello", "data", "By Livk"));

	@Autowired
	MockMvcTester tester;

	@Test
	void sendMsgDirect() {
		tester.post()
			.uri("/provider/sendMsgDirect")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body)
			.assertThat()
			.hasStatusOk();
	}

	@Test
	void sendMsgFanout() {
		tester.post()
			.uri("/provider/sendMsgFanout")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body)
			.assertThat()
			.hasStatusOk();
	}

	@Test
	void sendMsgTopic() {
		tester.post()
			.uri("/provider/sendMsgTopic/{key}", "rabbit.a.b")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body)
			.assertThat()
			.hasStatusOk();

		tester.post()
			.uri("/provider/sendMsgTopic/{key}", "a.b")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body)
			.assertThat()
			.hasStatusOk();
	}

	@Test
	void sendMsgHeaders() {
		tester.post()
			.uri("/provider/sendMsgHeaders")
			.param("json", "{\"auth\":\"livk\"}")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body)
			.assertThat()
			.hasStatusOk();

		tester.post()
			.uri("/provider/sendMsgHeaders")
			.param("json", "{\"username\":\"livk\",\"password\":\"livk\"}")
			.contentType(MediaType.APPLICATION_JSON)
			.content(body)
			.assertThat()
			.hasStatusOk();
	}

}
