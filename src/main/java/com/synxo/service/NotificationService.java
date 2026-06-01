package com.synxo.service;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.model.UserNotification;
import java.util.List;

public interface NotificationService {

	void createWelcomeNotification(Long recipientUserId);

	void createStateChangedNotification(Long recipientUserId, ProfileStateType state);

	void createMatchNotification(Long recipientUserId, Long matchedProfileId);

	void createMessageNotification(Long recipientUserId, Long senderUserId, String message);

	void createSystemNotification(Long recipientUserId, String message);

	List<UserNotification> getUnreadNotifications(String email);

	void dismissNotification(String email, Long notificationId);
}
