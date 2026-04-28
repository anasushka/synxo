package com.synxo.service;

import com.synxo.domain.enums.ProfileStateType;

public interface NotificationService {

	void createWelcomeNotification(Long recipientUserId);

	void createStateChangedNotification(Long recipientUserId, ProfileStateType state);

	void createMatchNotification(Long recipientUserId, Long matchedProfileId);

	void createMessageNotification(Long recipientUserId, Long senderUserId, String message);
}
