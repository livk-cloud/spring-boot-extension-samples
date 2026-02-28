package com.livk.modules.order;

import com.livk.modules.notification.Notification;
import com.livk.modules.notification.NotificationFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * OrderService
 * </p>
 *
 * @author livk
 * @date 2026/2/28
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class OrderService {

	private final NotificationFacadeService notificationFacadeService;

	public void createOrder(Order order) {
		log.info("Order Module: Attempting to create order.");
		// 1. 创建订单逻辑
		order.setNo(System.currentTimeMillis() + "");
		log.info("Order Module: Order created successfully, order number: {}", order.getNo());
		// 2. 发送通知
		// 订单模块通过NotificationFacadeService来发送通知，而不是直接调用Notification模块的内部实现
		Notification notification = new Notification("order_create",
				"Order created, order no is " + order.getNo() + ". Items: " + order.getItems());
		notificationFacadeService.send(notification);
		log.info("Order Module: Notification sent for order creation.");
	}

}
