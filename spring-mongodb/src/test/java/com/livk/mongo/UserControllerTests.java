package com.livk.mongo;

import com.livk.commons.jackson.JsonMapperUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    MockMvc mockMvc;

    @Test
    @Order(1)
    void save() throws Exception {
        User user = new User()
                .setName("root")
                .setAge(17);
        mockMvc.perform(post("/user")
                        .content(JsonMapperUtils.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("root"))
                .andExpect(jsonPath("$.age").value(17));
    }

    @Test
    @Order(3)
    void update() throws Exception {
        byte[] body = mockMvc.perform(get("/user/query?name=root"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        User user = JsonMapperUtils.readValue(body, User.class);
        user.setAge(18);
        mockMvc.perform(put("/user/{id}", user.getId())
                        .content(JsonMapperUtils.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("root"))
                .andExpect(jsonPath("$.age").value(18));
    }

    @Test
    @Order(2)
    void findByName() throws Exception {
        mockMvc.perform(get("/user/query?name=root"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("root"))
                .andExpect(jsonPath("$.age").value(17));
    }

    @Test
    @Order(4)
    void list() throws Exception {
        mockMvc.perform(get("/user"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].name").value("root"))
                .andExpect(jsonPath("[0].age").value(18));
    }

    @Test
    void remove() throws Exception {
        byte[] body = mockMvc.perform(get("/user/query?name=root"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        User user = JsonMapperUtils.readValue(body, User.class);
        mockMvc.perform(delete("/user/{id}", user.getId()))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
