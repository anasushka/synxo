package com.synxo.web.dto.response;

import java.time.LocalDateTime;

public record UserResponse(
	Long id,
	String email,
	String displayName,
	Integer age,
	String role,
	LocalDateTime createdAt
) {
}
