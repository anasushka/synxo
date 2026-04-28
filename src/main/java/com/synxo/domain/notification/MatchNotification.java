package com.synxo.domain.notification;

import com.synxo.domain.enums.NotificationType;
import java.time.LocalDateTime;

public class MatchNotification extends Notification {

	private final Long matchedProfileId;

	public MatchNotification(Long recipientUserId, Long matchedProfileId, String message, LocalDateTime createdAt) {
		super(NotificationType.MATCH, recipientUserId, message, createdAt);
		this.matchedProfileId = matchedProfileId;
	}

	public Long getMatchedProfileId() {
		return matchedProfileId;
	}
}
