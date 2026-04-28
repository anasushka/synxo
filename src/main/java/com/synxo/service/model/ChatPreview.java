package com.synxo.service.model;

import java.time.LocalDateTime;

public record ChatPreview(
	Long userId,
	String displayName,
	String photoUrl,
	String lastMessage,
	LocalDateTime lastMessageAt,
	boolean outgoing
) {
}
