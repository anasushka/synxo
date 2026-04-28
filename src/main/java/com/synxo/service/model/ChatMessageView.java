package com.synxo.service.model;

import java.time.LocalDateTime;

public record ChatMessageView(
	Long id,
	Long senderUserId,
	Long recipientUserId,
	String content,
	LocalDateTime createdAt,
	boolean outgoing
) {
}
