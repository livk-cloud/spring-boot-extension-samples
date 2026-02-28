package com.livk.modules.notification.internal;

import com.livk.modules.notification.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <p>
 * NotificationService
 * </p>
 *
 * @author livk
 * @date 2026/2/28
 */
@Slf4j
@Component
public class NotificationService {

	public void send(Notification notification) {
		String message = String.format("Notification for type '%s' with content: '%s'", notification.getType(),
				notification.getContent());
		log.info("Notification Module (Internal): Sending notification internally: {}", message);
	}

}
