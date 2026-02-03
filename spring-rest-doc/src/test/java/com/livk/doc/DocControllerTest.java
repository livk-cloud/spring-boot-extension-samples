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

package com.livk.doc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author livk
 */
@WebMvcTest
@ExtendWith({ SpringExtension.class, RestDocumentationExtension.class })
class DocControllerTest {

	MockMvcTester tester;

	@Autowired
	ObjectMapper mapper;

	@BeforeEach
	public void setMockMvc(WebApplicationContext webApplicationContext, RestDocumentationContextProvider provider) {
		this.tester = MockMvcTester.from(webApplicationContext,
				builder -> builder.apply(MockMvcRestDocumentation.documentationConfiguration(provider)).build());
	}

	@Test
	void testGet() {
		tester.get()
			.uri("/doc")
			.param("name", "world")
			.assertThat()
			.debug()
			.hasStatusOk()
			.matches(status().isOk())
			.matches(content().string("hello world"));
	}

	@Test
	void testPost() {
		var map = Map.of("username", "livk", "password", "123456");
		tester.post()
			.uri("/doc")
			.contentType(MediaType.APPLICATION_JSON)
			.content(mapper.writeValueAsString(map))
			.assertThat()
			.debug()
			.hasStatusOk()
			.matches(jsonPath("$.username").value("livk"))
			.matches(jsonPath("$.password").value("123456"));
	}

}
