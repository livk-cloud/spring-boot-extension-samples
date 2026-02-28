package com.livk.modules.notification;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <p>
 * Notification
 * </p>
 *
 * @author livk
 * @date 2026/2/28
 */
@Data
@AllArgsConstructor
public class Notification {

	private String type;

	private String content;

}
