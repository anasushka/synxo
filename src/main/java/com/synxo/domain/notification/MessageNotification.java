package com.synxo.domain.notification;

import com.synxo.domain.enums.NotificationType;
import java.time.LocalDateTime;

public class MessageNotification extends Notification {

	private final Long senderUserId;

	public MessageNotification(Long recipientUserId, Long senderUserId, String message, LocalDateTime createdAt) {
		super(NotificationType.MESSAGE, recipientUserId, message, createdAt);
		this.senderUserId = senderUserId;
	}

	public Long getSenderUserId() {
		return senderUserId;
	}
}
