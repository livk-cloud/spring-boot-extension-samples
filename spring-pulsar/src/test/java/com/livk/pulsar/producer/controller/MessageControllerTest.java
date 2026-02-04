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

package com.livk.pulsar.producer.controller;

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
import org.testcontainers.pulsar.PulsarContainer;

import java.time.Duration;
import java.util.Map;

/**
 * @author livk
 */
@SpringBootTest({ "spring.pulsar.consumer.topics=livk-topic", "spring.pulsar.consumer.subscription.name=consumer" })
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true, parallel = true)
class MessageControllerTest {

	@Container
	@ServiceConnection
	static final PulsarContainer pulsar = new PulsarContainer(DockerImageNames.pulsar()).withStartupAttempts(2)
		.withStartupTimeout(Duration.ofMinutes(3))
		.withExposedPorts(PulsarContainer.BROKER_PORT, PulsarContainer.BROKER_HTTP_PORT);

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("spring.pulsar.client.service-url",
				() -> "pulsar://" + pulsar.getHost() + ":" + pulsar.getFirstMappedPort());
	}

	@Autowired
	MockMvcTester tester;

	@Test
	void testSend() {
		var map = Map.of("username", "livk", "password", "123456");
		tester.post()
			.uri("/producer")
			.contentType(MediaType.APPLICATION_JSON)
			.content(JsonMapperUtils.writeValueAsString(map))
			.assertThat()
			.hasStatusOk();
	}

}
