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

package com.livk.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAcknowledgment;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author livk
 */
@Slf4j
@Component
public class OrderListener {

	@RabbitListener(queues = RabbitConfig.dealQueueOrder, ackMode = "MANUAL")
	public void processFail(String order, AmqpAcknowledgment acknowledgment) {
		acknowledgment.acknowledge();
		log.error("【订单号】 - [{}]被取消", order);
	}

	@RabbitListener(queues = RabbitConfig.orderQueue, ackMode = "MANUAL")
	public void processSuc(String order, AmqpAcknowledgment acknowledgment) throws InterruptedException {
		log.info("【订单号】 - [{}]", order);
		TimeUnit.SECONDS.sleep(1);
		// 是否支付
		var i = ThreadLocalRandom.current().nextInt(1, 100);
		if (i == 6) {
			acknowledgment.acknowledge();
			log.info("支付成功！");
		}
		else {
			acknowledgment.acknowledge(AmqpAcknowledgment.Status.REQUEUE);
			log.warn("缺少支付！");
		}
	}

}
