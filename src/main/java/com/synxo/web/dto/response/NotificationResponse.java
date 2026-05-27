package com.synxo.web.dto.response;

import com.synxo.domain.enums.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(
	Long id,
	NotificationType type,
	String message,
	LocalDateTime createdAt
) {
}
