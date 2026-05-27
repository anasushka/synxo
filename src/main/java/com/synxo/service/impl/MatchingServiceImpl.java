package com.synxo.service.impl;

import com.synxo.domain.enums.MatchingMode;
import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.exception.ResourceNotFoundException;
import com.synxo.domain.model.Profile;
import com.synxo.domain.strategy.MatchingContext;
import com.synxo.domain.strategy.MatchingStrategy;
import com.synxo.domain.strategy.ScoredProfile;
import com.synxo.repository.ProfileRepository;
import com.synxo.service.ProfileLikeService;
import com.synxo.service.MatchingService;
import com.synxo.service.model.LikeSnapshot;
import com.synxo.service.model.MatchResult;
import com.synxo.service.util.ServiceUtils;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
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
	public List<MatchResult> findMatches(String email, MatchingMode mode, int page, int size) {
		Profile source = profileRepository.findByUserEmail(ServiceUtils.normalizeEmail(email))
			.orElseThrow(() -> new ResourceNotFoundException("Profile for %s not found".formatted(email)));

		source.markActive();
		profileRepository.save(source);

		List<Profile> rawCandidates = findCandidatePool(source);
		List<Profile> candidates = source.search(rawCandidates);
		MatchingStrategy strategy = strategies.getOrDefault(mode, strategies.get(MatchingMode.RECOMMENDATION));
		LikeSnapshot snapshot = profileLikeService.getSnapshot(source.getUser().getId());
		MatchingContext context = new MatchingContext(snapshot.likedUserIds(), snapshot.likedByUserIds());

		List<MatchResult> ranked = strategy.rank(source, candidates, context).stream()
			.map(scoredProfile -> toMatchResult(source, scoredProfile, snapshot))
			.toList();

		int from = page * size;
		int to = Math.min(from + size, ranked.size());
		return from >= ranked.size() ? List.of() : ranked.subList(from, to);
	}

	@Override
	public MatchResult likeProfile(String email, Long targetUserId) {
		Profile source = profileRepository.findByUserEmail(ServiceUtils.normalizeEmail(email))
			.orElseThrow(() -> new ResourceNotFoundException("Profile for %s not found".formatted(email)));
		Profile candidate = profileRepository.findByUserId(targetUserId)
			.orElseThrow(() -> new ResourceNotFoundException("Profile for user id %s not found".formatted(targetUserId)));

		source.markActive();
		profileRepository.save(source);

		profileLikeService.like(source.getUser().getId(), targetUserId);
		LikeSnapshot snapshot = profileLikeService.getSnapshot(source.getUser().getId());
		MatchingContext context = new MatchingContext(snapshot.likedUserIds(), snapshot.likedByUserIds());
		MatchingStrategy strategy = strategies.getOrDefault(MatchingMode.RECOMMENDATION, strategies.values().iterator().next());
		ScoredProfile scoredProfile = strategy.rank(source, List.of(candidate), context).getFirst();
		return toMatchResult(source, scoredProfile, snapshot);
	}

	private List<Profile> findCandidatePool(Profile source) {
		LocalDateTime activeAfter = source.requiresRecentlyActiveCandidates()
			? LocalDateTime.now().minusDays(7)
			: null;
		int minimumSharedInterests = source.minimumSharedInterestsForSearch();

		if (minimumSharedInterests <= 0) {
			return profileRepository.findVisibleCandidates(
				source.getUser().getId(),
				ProfileStateType.GHOST_MODE,
				activeAfter
			);
		}

		if (source.getInterests() == null || source.getInterests().isEmpty()) {
			return List.of();
		}

		return profileRepository.findVisibleCandidatesBySharedInterests(
			source.getUser().getId(),
			ProfileStateType.GHOST_MODE,
			activeAfter,
			source.getInterests(),
			minimumSharedInterests
		);
	}

	private MatchResult toMatchResult(Profile source, ScoredProfile scoredProfile, LikeSnapshot snapshot) {
		Profile candidate = scoredProfile.profile();
		return toMatchResult(
			source,
			scoredProfile,
			snapshot.likedByYou(candidate.getUser().getId()),
			snapshot.likedYou(candidate.getUser().getId()),
			snapshot.mutualLike(candidate.getUser().getId())
		);
	}

	private MatchResult toMatchResult(Profile source, ScoredProfile scoredProfile, boolean likedByYou, boolean likedYou, boolean mutualLike) {
		Profile candidate = scoredProfile.profile();
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
			scoredProfile.score().total(),
			scoredProfile.score().interest(),
			scoredProfile.score().distance(),
			scoredProfile.score().intention(),
			scoredProfile.score().activity(),
			scoredProfile.score().social(),
			likedByYou,
			likedYou,
			mutualLike
		);
	}

	private Double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
}
