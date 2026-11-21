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

package com.livk.graphql.mybatis.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.WebSocketGraphQlTester;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author livk
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.url=jdbc:h2:mem:test",
				"spring.sql.init.platform=h2", "spring.sql.init.mode=embedded" })
class GreetingControllerTest {

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
