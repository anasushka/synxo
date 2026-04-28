package com.synxo.web.dto.response;

import java.time.LocalDateTime;

public record ChatMessageResponse(
	Long id,
	Long senderUserId,
	Long recipientUserId,
	String content,
	LocalDateTime createdAt,
	boolean outgoing
) {
}
