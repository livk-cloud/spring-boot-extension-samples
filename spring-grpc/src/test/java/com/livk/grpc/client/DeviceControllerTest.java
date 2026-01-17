package com.livk.grpc.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author livk
 */
@SpringBootTest
@AutoConfigureWebTestClient
class DeviceControllerTest {

	@Autowired
	WebTestClient client;

	@Test
	void query() {
		client.get()
			.uri(builder -> builder.path("/grpc/device").queryParam("name", "Air Pod 3").build())
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("name")
			.isEqualTo("Air Pod 3")
			.jsonPath("mac")
			.isEqualTo("00:00:00:00");

		client.get()
			.uri(builder -> builder.path("/grpc/device").queryParam("name", "Air Pod Pro").build())
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("name")
			.isEqualTo("Air Pod Pro")
			.jsonPath("mac")
			.isEqualTo("01:02:03:04");
	}

	@Test
	void add() {
		var dto = new DeviceDTO();
		dto.setName("Air Pod Pro 2");
		dto.setMac("11:22:33:44");

		client.post()
			.uri("/grpc/device")
			.bodyValue(dto)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(Boolean.class)
			.isEqualTo(true);

		client.post()
			.uri("/grpc/device")
			.bodyValue(dto)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(Boolean.class)
			.isEqualTo(false);

		client.get()
			.uri(builder -> builder.path("/grpc/device").queryParam("name", dto.getName()).build())
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.jsonPath("name")
			.isEqualTo(dto.getName())
			.jsonPath("mac")
			.isEqualTo(dto.getMac());
	}

}
