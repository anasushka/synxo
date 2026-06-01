package com.synxo.web.dto.response;

import java.time.LocalDateTime;

public record AchievementResponse(
	String code,
	String title,
	String description,
	LocalDateTime unlockedAt
) {
}
