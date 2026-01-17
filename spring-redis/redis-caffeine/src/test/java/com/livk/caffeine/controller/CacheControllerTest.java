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
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
	MockMvc mockMvc;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@Test
	void testGet() throws Exception {
		var result = new HashSet<>();
		var uuid = mockMvc.perform(get("/cache"))
			.andExpect(status().isOk())
			.andDo(print())
			.andReturn()
			.getResponse()
			.getContentAsString();
		result.add(uuid);
		for (var i = 0; i < 3; i++) {
			var newUUID = mockMvc.perform(get("/cache"))
				.andExpect(status().isOk())
				.andDo(print())
				.andExpect(content().string(uuid))
				.andReturn()
				.getResponse()
				.getContentAsString();
			result.add(newUUID);
		}
		assertThat(result).hasSize(1);
	}

	@Test
	void testPut() throws Exception {
		var result = new HashSet<>();
		for (var i = 0; i < 3; i++) {
			var uuid = mockMvc.perform(post("/cache"))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn()
				.getResponse()
				.getContentAsString();
			result.add(uuid);
			var newUUID = mockMvc.perform(get("/cache"))
				.andExpect(status().isOk())
				.andDo(print())
				.andExpect(content().string(uuid))
				.andReturn()
				.getResponse()
				.getContentAsString();
			result.add(newUUID);
		}
		assertThat(result).hasSize(3);
	}

	@Test
	void testDelete() throws Exception {
		mockMvc.perform(delete("/cache")).andExpect(status().isOk()).andDo(print()).andExpect(content().string("over"));
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
