package com.synxo.web.controller;

import com.synxo.domain.enums.MatchingMode;
import com.synxo.service.MatchingService;
import com.synxo.web.dto.response.MatchResponse;
import com.synxo.web.mapper.ApiMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchingController {

	private final MatchingService matchingService;
	private final ApiMapper apiMapper;

	@GetMapping
	public List<MatchResponse> findMatches(
		Authentication authentication,
		@RequestParam(defaultValue = "RECOMMENDATION") MatchingMode strategy
	) {
		return matchingService.findMatches(authentication.getName(), strategy).stream()
			.map(apiMapper::toMatchResponse)
			.toList();
	}

	@PostMapping("/{userId}/like")
	public MatchResponse likeProfile(Authentication authentication, @PathVariable Long userId) {
		return apiMapper.toMatchResponse(matchingService.likeProfile(authentication.getName(), userId));
	}
}
