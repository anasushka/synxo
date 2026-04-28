package com.synxo.service.impl;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.notification.MatchNotification;
import com.synxo.domain.notification.MessageNotification;
import com.synxo.domain.notification.SystemNotification;
import com.synxo.infrastructure.notification.NotificationFactory;
import com.synxo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final NotificationFactory notificationFactory;

	@Override
	public SystemNotification createWelcomeNotification(Long recipientUserId) {
		SystemNotification notification = notificationFactory.createSystemNotification(recipientUserId, "Welcome to Synxo.");
		log.info("Prepared welcome notification for user {}", recipientUserId);
		return notification;
	}

	@Override
	public SystemNotification createStateChangedNotification(Long recipientUserId, ProfileStateType state) {
		SystemNotification notification = notificationFactory.createSystemNotification(
			recipientUserId,
			"Your profile state was updated to %s.".formatted(state.name())
		);
		log.info("Prepared state update notification for user {}", recipientUserId);
		return notification;
	}

	@Override
	public MatchNotification createMatchNotification(Long recipientUserId, Long matchedProfileId) {
		MatchNotification notification = notificationFactory.createMatchNotification(recipientUserId, matchedProfileId);
		log.info("Prepared match notification for user {}", recipientUserId);
		return notification;
	}

	@Override
	public MessageNotification createMessageNotification(Long recipientUserId, Long senderUserId, String message) {
		MessageNotification notification = notificationFactory.createMessageNotification(recipientUserId, senderUserId, message);
		log.info("Prepared message notification for user {}", recipientUserId);
		return notification;
	}
}
