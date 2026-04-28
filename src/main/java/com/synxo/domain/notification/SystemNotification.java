package com.synxo.domain.notification;

import com.synxo.domain.enums.NotificationType;
import java.time.LocalDateTime;

public class SystemNotification extends Notification {

	public SystemNotification(Long recipientUserId, String message, LocalDateTime createdAt) {
		super(NotificationType.SYSTEM, recipientUserId, message, createdAt);
	}
}
