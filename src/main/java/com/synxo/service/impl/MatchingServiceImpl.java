package com.synxo.service.impl;

import com.synxo.domain.enums.MatchingMode;
import com.synxo.domain.exception.ResourceNotFoundException;
import com.synxo.domain.model.Profile;
import com.synxo.domain.strategy.MatchingStrategy;
import com.synxo.repository.ProfileRepository;
import com.synxo.service.ProfileLikeService;
import com.synxo.service.MatchingService;
import com.synxo.service.model.LikeResult;
import com.synxo.service.model.LikeSnapshot;
import com.synxo.service.model.MatchResult;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MatchingServiceImpl implements MatchingService {

	private final ProfileRepository profileRepository;
	private final ProfileLikeService profileLikeService;
	private final Map<MatchingMode, MatchingStrategy> strategies = new EnumMap<>(MatchingMode.class);

	public MatchingServiceImpl(
		ProfileRepository profileRepository,
		ProfileLikeService profileLikeService,
		List<MatchingStrategy> matchingStrategies
	) {
		this.profileRepository = profileRepository;
		this.profileLikeService = profileLikeService;
		for (MatchingStrategy strategy : matchingStrategies) {
			strategies.put(strategy.getMode(), strategy);
		}
	}

	@Override
	public List<MatchResult> findMatches(String email, MatchingMode mode) {
		Profile source = profileRepository.findByUserEmail(normalizeEmail(email))
			.orElseThrow(() -> new ResourceNotFoundException("Profile for %s not found".formatted(email)));

		source.markActive();
		profileRepository.save(source);

		List<Profile> candidates = source.search(profileRepository.findByUserIdNot(source.getUser().getId())).stream()
			.filter(candidate -> source.commonInterestCount(candidate) > 0)
			.toList();
		MatchingStrategy strategy = strategies.getOrDefault(mode, strategies.get(MatchingMode.RECOMMENDATION));
		LikeSnapshot snapshot = profileLikeService.getSnapshot(source.getUser().getId());

		return strategy.rank(source, candidates).stream()
			.map(candidate -> toMatchResult(source, candidate, snapshot))
			.toList();
	}

	@Override
	public MatchResult likeProfile(String email, Long targetUserId) {
		Profile source = profileRepository.findByUserEmail(normalizeEmail(email))
			.orElseThrow(() -> new ResourceNotFoundException("Profile for %s not found".formatted(email)));
		Profile candidate = profileRepository.findByUserId(targetUserId)
			.orElseThrow(() -> new ResourceNotFoundException("Profile for user id %s not found".formatted(targetUserId)));

		source.markActive();
		profileRepository.save(source);

		LikeResult likeResult = profileLikeService.like(source.getUser().getId(), targetUserId);
		return toMatchResult(source, candidate, likeResult);
	}

	private MatchResult toMatchResult(Profile source, Profile candidate, LikeSnapshot snapshot) {
		return toMatchResult(
			source,
			candidate,
			snapshot.likedByYou(candidate.getUser().getId()),
			snapshot.likedYou(candidate.getUser().getId()),
			snapshot.mutualLike(candidate.getUser().getId())
		);
	}

	private MatchResult toMatchResult(Profile source, Profile candidate, LikeResult likeResult) {
		return toMatchResult(source, candidate, likeResult.likedByYou(), likeResult.likedYou(), likeResult.mutualLike());
	}

	private MatchResult toMatchResult(Profile source, Profile candidate, boolean likedByYou, boolean likedYou, boolean mutualLike) {
		double distance = source.distanceTo(candidate);
		Double normalizedDistance = distance == Double.MAX_VALUE ? null : round(distance);

		return new MatchResult(
			candidate.getId(),
			candidate.getUser().getId(),
			candidate.getUser().getDisplayName(),
			candidate.getPhotoUrl(),
			candidate.getUser().getAge(),
			candidate.getCity(),
			candidate.getState(),
			source.sharedInterests(candidate),
			normalizedDistance,
			likedByYou,
			likedYou,
			mutualLike
		);
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private Double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
}
