package com.synxo.infrastructure.notification;

import com.synxo.domain.notification.MatchNotification;
import com.synxo.domain.notification.MessageNotification;
import com.synxo.domain.notification.SystemNotification;

public interface NotificationFactory {

	MatchNotification createMatchNotification(Long recipientUserId, Long matchedProfileId);

	MessageNotification createMessageNotification(Long recipientUserId, Long senderUserId, String message);

	SystemNotification createSystemNotification(Long recipientUserId, String message);
}
