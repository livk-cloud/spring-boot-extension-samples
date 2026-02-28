package com.livk.modules.notification;

import com.livk.modules.notification.internal.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * NotificationFacadeService
 * </p>
 *
 * @author livk
 * @date 2026/2/28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationFacadeService {

	protected final NotificationService notificationService;

	public void send(Notification notification) {
		log.info("Notification Module (Facade): Received notification: {} - {}", notification.getType(),
				notification.getContent());
		// 调用内部服务处理通知
		notificationService.send(notification);
		log.info("Notification Module (Facade): Notification sent successfully!");
	}

}
