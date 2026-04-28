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
			"You have a new match in Synxo.",
			LocalDateTime.now()
		);
	}

	@Override
	public MessageNotification createMessageNotification(Long recipientUserId, Long senderUserId, String message) {
		return new MessageNotification(
			recipientUserId,
			senderUserId,
			message,
			LocalDateTime.now()
		);
	}

	@Override
	public SystemNotification createSystemNotification(Long recipientUserId, String message) {
		return new SystemNotification(recipientUserId, message, LocalDateTime.now());
	}
}
