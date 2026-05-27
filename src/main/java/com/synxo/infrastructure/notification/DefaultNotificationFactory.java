package com.synxo.infrastructure.notification;

import com.synxo.domain.notification.MatchNotification;
import com.synxo.domain.notification.MessageNotification;
import com.synxo.domain.notification.SystemNotification;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class DefaultNotificationFactory implements NotificationFactory {

	@Override
	public MatchNotification createMatchNotification(Long recipientUserId, Long matchedProfileId) {
		return new MatchNotification(
			recipientUserId,
			matchedProfileId,
			"Новая взаимная симпатия. Теперь можно открыть чат.",
			LocalDateTime.now()
		);
	}

	@Override
	public MessageNotification createMessageNotification(Long recipientUserId, Long senderUserId, String message) {
		return new MessageNotification(
			recipientUserId,
			senderUserId,
			"Новое сообщение: %s".formatted(shorten(message)),
			LocalDateTime.now()
		);
	}

	@Override
	public SystemNotification createSystemNotification(Long recipientUserId, String message) {
		return new SystemNotification(recipientUserId, message, LocalDateTime.now());
	}

	private String shorten(String message) {
		if (message == null || message.isBlank()) {
			return "без текста";
		}
		String value = message.trim();
		return value.length() > 80 ? value.substring(0, 80) + "..." : value;
	}
}
