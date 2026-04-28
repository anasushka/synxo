package com.synxo.web.dto.response;

import java.time.LocalDateTime;

public record ChatPreviewResponse(
	Long userId,
	String displayName,
	String photoUrl,
	String lastMessage,
	LocalDateTime lastMessageAt,
	boolean outgoing
) {
}
