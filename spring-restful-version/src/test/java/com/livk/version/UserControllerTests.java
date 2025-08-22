package com.livk.version;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author livk
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {

	@Autowired
	MockMvc mockMvc;

	@Test
	void v1() throws Exception {
		mockMvc.perform(get("/users").header("X-API-Version", "v1"))
			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(content().string("User v1"));
	}

	@Test
	void v2() throws Exception {
		mockMvc.perform(get("/users").header("X-API-Version", "v2"))
			.andExpect(status().isOk())
			.andDo(print())
			.andExpect(content().string("User v2"));
	}

}
