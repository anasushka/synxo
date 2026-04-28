package com.synxo.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
	@NotNull(message = "Recipient is required")
	Long recipientUserId,

	@NotBlank(message = "Message cannot be empty")
	@Size(max = 2000, message = "Message must be shorter than 2000 characters")
	String content
) {
}
