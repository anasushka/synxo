package com.synxo.domain.notification;

import com.synxo.domain.enums.NotificationType;
import java.time.LocalDateTime;

public abstract class Notification {

	private final NotificationType type;
	private final Long recipientUserId;
	private final String message;
	private final LocalDateTime createdAt;

	protected Notification(NotificationType type, Long recipientUserId, String message, LocalDateTime createdAt) {
		this.type = type;
		this.recipientUserId = recipientUserId;
		this.message = message;
		this.createdAt = createdAt;
	}

	public NotificationType getType() {
		return type;
	}

	public Long getRecipientUserId() {
		return recipientUserId;
	}

	public String getMessage() {
		return message;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
