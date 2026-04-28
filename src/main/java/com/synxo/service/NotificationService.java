package com.synxo.service;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.notification.MatchNotification;
import com.synxo.domain.notification.MessageNotification;
import com.synxo.domain.notification.SystemNotification;

public interface NotificationService {

	SystemNotification createWelcomeNotification(Long recipientUserId);

	SystemNotification createStateChangedNotification(Long recipientUserId, ProfileStateType state);

	MatchNotification createMatchNotification(Long recipientUserId, Long matchedProfileId);

	MessageNotification createMessageNotification(Long recipientUserId, Long senderUserId, String message);
}
