package com.synxo.service.impl;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.exception.ResourceNotFoundException;
import com.synxo.domain.notification.Notification;
import com.synxo.domain.model.User;
import com.synxo.domain.model.UserNotification;
import com.synxo.infrastructure.notification.NotificationFactory;
import com.synxo.repository.UserNotificationRepository;
import com.synxo.repository.UserRepository;
import com.synxo.service.NotificationService;
import com.synxo.service.util.ServiceUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

	private final NotificationFactory notificationFactory;
	private final UserNotificationRepository userNotificationRepository;
	private final UserRepository userRepository;

	@Override
	public void createWelcomeNotification(Long recipientUserId) {
		createSystemNotification(recipientUserId, "Добро пожаловать в Synxo.");
		log.info("Prepared welcome notification for user {}", recipientUserId);
	}

	@Override
	public void createStateChangedNotification(Long recipientUserId, ProfileStateType state) {
		createSystemNotification(
			recipientUserId,
			"Состояние профиля обновлено: %s.".formatted(state.name())
		);
		log.info("Prepared state update notification for user {}", recipientUserId);
	}

	@Override
	public void createMatchNotification(Long recipientUserId, Long matchedProfileId) {
		save(notificationFactory.createMatchNotification(recipientUserId, matchedProfileId));
		log.info("Prepared match notification for user {}", recipientUserId);
	}

	@Override
	public void createMessageNotification(Long recipientUserId, Long senderUserId, String message) {
		save(notificationFactory.createMessageNotification(recipientUserId, senderUserId, message));
		log.info("Prepared message notification for user {}", recipientUserId);
	}

	@Override
	public void createSystemNotification(Long recipientUserId, String message) {
		save(notificationFactory.createSystemNotification(recipientUserId, message));
		log.info("Prepared system notification for user {}", recipientUserId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserNotification> getUnreadNotifications(String email) {
		User user = getUser(email);
		return userNotificationRepository.findByRecipientUserIdAndDismissedAtIsNullOrderByCreatedAtAscIdAsc(user.getId());
	}

	@Override
	public void dismissNotification(String email, Long notificationId) {
		User user = getUser(email);
		UserNotification notification = userNotificationRepository.findByIdAndRecipientUserId(notificationId, user.getId())
			.orElseThrow(() -> new ResourceNotFoundException("Notification with id %s not found".formatted(notificationId)));
		notification.dismiss();
		userNotificationRepository.save(notification);
	}

	private UserNotification save(Notification notification) {
		return userNotificationRepository.save(UserNotification.builder()
			.recipientUserId(notification.getRecipientUserId())
			.type(notification.getType())
			.message(notification.getMessage())
			.createdAt(notification.getCreatedAt())
			.build());
	}

	private User getUser(String email) {
		return userRepository.findByEmail(ServiceUtils.normalizeEmail(email))
			.orElseThrow(() -> new ResourceNotFoundException("User with email %s not found".formatted(email)));
	}
}
