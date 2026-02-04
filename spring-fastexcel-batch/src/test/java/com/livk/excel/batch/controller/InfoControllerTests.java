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

package com.livk.excel.batch.controller;

import com.livk.testcontainers.DockerImageNames;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author livk
 */
@SpringBootTest({ "spring.sql.init.mode=always",
		"spring.sql.init.schema-locations=classpath:org/springframework/batch/core/schema-mysql.sql" })
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true, parallel = true)
class InfoControllerTests {

	@Container
	@ServiceConnection
	static final MySQLContainer mysql = new MySQLContainer(DockerImageNames.mysql())
		.withEnv("MYSQL_ROOT_PASSWORD", "123456")
		.withDatabaseName("fastexcel_batch");

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		registry.add("spring.datasource.url", () -> "jdbc:mysql://" + mysql.getHost() + ":" + mysql.getFirstMappedPort()
				+ "/" + mysql.getDatabaseName() + "?createDatabaseIfNotExist=true");
	}

	@Autowired
	MockMvcTester tester;

	@Test
	void upload() throws Exception {
		var resource = new ClassPathResource("mobile-test.xlsx");
		var file = new MockMultipartFile("file", "mobile-test.xlsx", MediaType.MULTIPART_FORM_DATA_VALUE,
				resource.getInputStream());
		tester.post().uri("/upload").multipart().file(file).assertThat().hasStatusOk();
	}

	@Test
	void up() throws Exception {
		var resource = new ClassPathResource("mobile-test.xlsx");

		var file = new MockMultipartFile("file", "mobile-test.xlsx", MediaType.MULTIPART_FORM_DATA_VALUE,
				resource.getInputStream());

		tester.post()
			.uri("/excel")
			.multipart()
			.file(file)
			.assertThat()
			.hasStatusOk()
			.matches(jsonPath("$.length()").value(100000))
			.matches(jsonPath("$[99999]").exists());
	}

}
