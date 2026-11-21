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

package com.livk.mqtt.config;

import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.client.core.Mqtt3ClientManager;
import org.springframework.util.Assert;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * @author livk
 */
@Configuration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttConfig {

	public static final String SENDER_CLIENT_MANAGER = "mqttSenderClientManager";

	public static final String RECEIVER_CLIENT_MANAGER = "mqttReceiverClientManager";

	@Bean(name = SENDER_CLIENT_MANAGER)
	public Mqtt3ClientManager mqttSenderClientManager(MqttProperties properties) {
		return createClientManager(properties, properties.getSender().getClientId());
	}

	@Bean(name = RECEIVER_CLIENT_MANAGER)
	public Mqtt3ClientManager mqttReceiverClientManager(MqttProperties properties) {
		return createClientManager(properties, properties.getReceiver().getClientId());
	}

	private static Mqtt3ClientManager createClientManager(MqttProperties properties, String clientId) {
		Assert.hasText(clientId, "MQTT clientId must not be empty");
		var broker = URI.create(properties.getUrl().split(";")[0].trim());
		var host = broker.getHost();
		Assert.hasText(host, "MQTT broker host must not be empty");
		var port = broker.getPort() > 0 ? broker.getPort() : 1883;

		var clientConfig = Mqtt3Client.builder()
			.identifier(clientId)
			.serverHost(host)
			.serverPort(port)
			.simpleAuth()
			.username(properties.getUsername())
			.password(properties.getPassword().getBytes(StandardCharsets.UTF_8))
			.applySimpleAuth()
			.automaticReconnectWithDefaultConfig()
			.build()
			.getConfig();

		var manager = new Mqtt3ClientManager(clientConfig);
		manager.setMqttConnect(Mqtt3Connect.builder().keepAlive(properties.getKeepAliveInterval()).build());
		return manager;
	}

}
