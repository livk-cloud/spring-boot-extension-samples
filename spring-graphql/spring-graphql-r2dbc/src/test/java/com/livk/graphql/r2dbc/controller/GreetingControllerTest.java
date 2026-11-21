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

package com.livk.graphql.r2dbc.controller;

import com.livk.testcontainers.DockerImageNames;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.graphql.test.tester.WebSocketGraphQlTester;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author livk
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true, parallel = true)
class GreetingControllerTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer postgresql = new PostgreSQLContainer(DockerImageNames.postgres())
		.withEnv("POSTGRES_PASSWORD", "123456")
		.withDatabaseName("graphql");

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("spring.r2dbc.username", postgresql::getUsername);
		registry.add("spring.r2dbc.password", postgresql::getPassword);
		registry.add("spring.r2dbc.url", () -> "r2dbc:postgres://" + postgresql.getHost() + ":"
				+ postgresql.getFirstMappedPort() + "/" + postgresql.getDatabaseName());
	}

	@LocalServerPort
	int port;

	@Value("${spring.graphql.websocket.path:${spring.graphql.path:/graphql}}")
	String graphqlPath;

	WebSocketGraphQlTester tester;

	@BeforeEach
	void init() {
		var url = "http://localhost:" + this.port + this.graphqlPath;
		this.tester = WebSocketGraphQlTester.builder(url, new ReactorNettyWebSocketClient()).build();
	}

	@AfterEach
	void tearDown() {
		if (this.tester != null) {
			this.tester.stop().block(Duration.ofSeconds(5));
		}
	}

	@Test
	void greetings() {
		// language=GraphQL
		var document = """
				subscription {
				  greetings
				}""";
		var greeting = this.tester.document(document)
			.executeSubscription()
			.toFlux("greetings", String.class)
			.next()
			.block(Duration.ofSeconds(15));

		assertThat(greeting).isNotNull().startsWith("Hello GraphQL ");
	}

}
