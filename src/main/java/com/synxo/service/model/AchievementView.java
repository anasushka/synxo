package com.synxo.service.model;

import java.time.LocalDateTime;

public record AchievementView(
	String code,
	String title,
	String description,
	LocalDateTime unlockedAt
) {
}
