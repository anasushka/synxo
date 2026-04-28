package com.synxo.service.impl;

import com.synxo.domain.enums.ProfileStateType;
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
	public void createWelcomeNotification(Long recipientUserId) {
		notificationFactory.createSystemNotification(recipientUserId, "Welcome to Synxo.");
		log.info("Prepared welcome notification for user {}", recipientUserId);
	}

	@Override
	public void createStateChangedNotification(Long recipientUserId, ProfileStateType state) {
		notificationFactory.createSystemNotification(
			recipientUserId,
			"Your profile state was updated to %s.".formatted(state.name())
		);
		log.info("Prepared state update notification for user {}", recipientUserId);
	}

	@Override
	public void createMatchNotification(Long recipientUserId, Long matchedProfileId) {
		notificationFactory.createMatchNotification(recipientUserId, matchedProfileId);
		log.info("Prepared match notification for user {}", recipientUserId);
	}

	@Override
	public void createMessageNotification(Long recipientUserId, Long senderUserId, String message) {
		notificationFactory.createMessageNotification(recipientUserId, senderUserId, message);
		log.info("Prepared message notification for user {}", recipientUserId);
	}
}
