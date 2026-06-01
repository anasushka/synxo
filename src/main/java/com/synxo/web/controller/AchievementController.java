package com.synxo.web.controller;

import com.synxo.service.AchievementService;
import com.synxo.web.dto.response.AchievementResponse;
import com.synxo.web.mapper.ApiMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

	private final AchievementService achievementService;
	private final ApiMapper apiMapper;

	@GetMapping
	public List<AchievementResponse> getAchievements(Authentication authentication) {
		return achievementService.getAchievements(authentication.getName()).stream()
			.map(apiMapper::toAchievementResponse)
			.toList();
	}
}
