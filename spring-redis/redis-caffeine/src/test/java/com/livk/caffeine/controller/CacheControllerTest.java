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

package com.livk.caffeine.controller;

import com.livk.testcontainers.DockerImageNames;
import com.redis.testcontainers.RedisContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author livk
 */
@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true, parallel = true)
class CacheControllerTest {

	@Container
	@ServiceConnection
	static final RedisContainer redis = new RedisContainer(DockerImageNames.redis());

	@DynamicPropertySource
	static void redisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", redis::getFirstMappedPort);
	}

	@Autowired
	MockMvcTester tester;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@Test
	void testGet() {
		var result = new HashSet<>();
		var uuid = tester.get().uri("/cache").assertThat().debug().hasStatusOk().bodyText().actual();
		result.add(uuid);
		for (var i = 0; i < 3; i++) {
			var newUUID = tester.get().uri("/cache").assertThat().debug().hasStatusOk().bodyText().actual();
			result.add(newUUID);
		}
		assertThat(result).hasSize(1);
	}

	@Test
	void testPut() {
		var result = new HashSet<>();
		for (var i = 0; i < 3; i++) {
			var uuid = tester.post().uri("/cache").assertThat().debug().hasStatusOk().bodyText().actual();
			result.add(uuid);
			var newUUID = tester.get().uri("/cache").assertThat().debug().hasStatusOk().bodyText().actual();
			result.add(newUUID);
		}
		assertThat(result).hasSize(3);
	}

	@Test
	void testDelete() {
		tester.delete().uri("/cache").assertThat().debug().hasStatusOk().bodyText().isEqualTo("over");
	}

	@Test
	void test() {
		var options = ScanOptions.scanOptions().match("*").count(100).build();
		try (var cursor = redisTemplate.scan(options)) {
			while (cursor.hasNext()) {
				log.info("key:{} cursorId:{} position:{}", cursor.next(), cursor.getId(), cursor.getPosition());
			}
		}

		try (var scan = redisTemplate.scan(options)) {
			var keys = scan.stream().limit(1).collect(Collectors.toSet());
			log.info("keys:{}", keys);
			assertThat(keys).hasSize(1);
		}
	}

}
