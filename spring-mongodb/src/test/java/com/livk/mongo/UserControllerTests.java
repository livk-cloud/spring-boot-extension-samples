package com.livk.mongo;

import com.livk.commons.jackson.JsonMapperUtils;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author livk
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Testcontainers(disabledWithoutDocker = true)
class UserControllerTests {

	@Container
	@ServiceConnection
	static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.host", mongo::getHost);
		registry.add("spring.data.mongodb.port", mongo::getFirstMappedPort);
	}

	@Autowired
	MockMvcTester tester;

	@Test
	@Order(1)
	void save() {
		var user = new User().setName("root").setAge(17);
		tester.post()
			.uri("/user")
			.contentType(MediaType.APPLICATION_JSON)
			.content(JsonMapperUtils.writeValueAsString(user))
			.assertThat()
			.debug()
			.hasStatusOk()
			.matches(jsonPath("$.name").value("root"))
			.matches(jsonPath("$.age").value(17));
	}

	@Test
	@Order(3)
	void update() {
		var user = tester.get()
			.uri("/user/query?name=root")
			.assertThat()
			.debug()
			.hasStatusOk()
			.bodyJson()
			.convertTo(User.class)
			.actual();
		user.setAge(18);
		tester.put()
			.uri("/user/{id}", user.getId())
			.contentType(MediaType.APPLICATION_JSON)
			.content(JsonMapperUtils.writeValueAsString(user))
			.assertThat()
			.debug()
			.hasStatusOk()
			.matches(jsonPath("$.name").value("root"))
			.matches(jsonPath("$.age").value(18));
	}

	@Test
	@Order(2)
	void findByName() {
		tester.get()
			.uri("/user/query?name=root")
			.assertThat()
			.debug()
			.hasStatusOk()
			.matches(jsonPath("$.name").value("root"))
			.matches(jsonPath("$.age").value(17));
	}

	@Test
	@Order(4)
	void list() {
		tester.get()
			.uri("/user")
			.assertThat()
			.debug()
			.hasStatusOk()
			.matches(jsonPath("[0].name").value("root"))
			.matches(jsonPath("[0].age").value(18));
	}

	@Test
	void remove() {
		var user = tester.get()
			.uri("/user/query?name=root")
			.assertThat()
			.debug()
			.hasStatusOk()
			.bodyJson()
			.convertTo(User.class)
			.actual();
		tester.delete().uri("/user/{id}", user.getId()).assertThat().debug().hasStatusOk();
	}

}
