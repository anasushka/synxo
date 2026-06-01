package com.synxo.service.impl;

import com.synxo.domain.enums.MatchingMode;
import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.exception.ResourceNotFoundException;
import com.synxo.domain.model.Profile;
import com.synxo.domain.strategy.MatchScore;
import com.synxo.domain.strategy.MatchingContext;
import com.synxo.domain.strategy.MatchingStrategy;
import com.synxo.domain.strategy.ScoredProfile;
import com.synxo.repository.ProfileRepository;
import com.synxo.service.AchievementService;
import com.synxo.service.ProfileLikeService;
import com.synxo.service.MatchingService;
import com.synxo.service.model.DailyMatchResult;
import com.synxo.service.model.LikeSnapshot;
import com.synxo.service.model.MatchResult;
import com.synxo.service.util.ServiceUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchingServiceImpl implements MatchingService {

	private static final int MINIMUM_SHARED_INTERESTS = 1;

	private final ProfileRepository profileRepository;
	private final ProfileLikeService profileLikeService;
	private final AchievementService achievementService;
	private final Map<MatchingMode, MatchingStrategy> strategies = new EnumMap<>(MatchingMode.class);

	public MatchingServiceImpl(
		ProfileRepository profileRepository,
		ProfileLikeService profileLikeService,
		AchievementService achievementService,
		List<MatchingStrategy> matchingStrategies
	) {
		this.profileRepository = profileRepository;
		this.profileLikeService = profileLikeService;
		this.achievementService = achievementService;
		for (MatchingStrategy strategy : matchingStrategies) {
			strategies.put(strategy.getMode(), strategy);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<MatchResult> findMatches(String email, MatchingMode mode, int page, int size) {
		Profile source = getProfile(email);
		List<MatchResult> ranked = rankMatches(source, mode);

		int from = page * size;
		int to = Math.min(from + size, ranked.size());
		return from >= ranked.size() ? List.of() : ranked.subList(from, to);
	}

	@Override
	@Transactional(readOnly = true)
	public DailyMatchResult findDailyMatch(String email, MatchingMode mode) {
		Profile source = getProfile(email);
		List<MatchResult> ranked = rankMatches(source, mode);
		if (ranked.isEmpty()) {
			return new DailyMatchResult(LocalDate.now(), null);
		}

		int poolSize = Math.min(3, ranked.size());
		int rotationSeed = source.getUser().getId() == null ? 0 : source.getUser().getId().intValue();
		int index = Math.floorMod(LocalDate.now().getDayOfYear() + rotationSeed, poolSize);
		return new DailyMatchResult(LocalDate.now(), ranked.get(index));
	}

	@Override
	@Transactional
	public MatchResult likeProfile(String email, Long targetUserId) {
		Profile source = getProfile(email);
		Profile candidate = profileRepository.findByUserId(targetUserId)
			.orElseThrow(() -> new ResourceNotFoundException("Profile for user id %s not found".formatted(targetUserId)));

		source.markActive();
		profileRepository.save(source);
		achievementService.evaluateForUser(source.getUser().getId());

		profileLikeService.like(source.getUser().getId(), targetUserId);
		LikeSnapshot snapshot = profileLikeService.getSnapshot(source.getUser().getId());
		MatchingContext context = new MatchingContext(snapshot.likedUserIds(), snapshot.likedByUserIds());
		MatchingStrategy strategy = strategies.getOrDefault(MatchingMode.RECOMMENDATION, strategies.values().iterator().next());
		ScoredProfile scoredProfile = strategy.rank(source, List.of(candidate), context).getFirst();
		return toMatchResult(source, scoredProfile, snapshot);
	}

	private Profile getProfile(String email) {
		return profileRepository.findByUserEmail(ServiceUtils.normalizeEmail(email))
			.orElseThrow(() -> new ResourceNotFoundException("Profile for %s not found".formatted(email)));
	}

	private List<MatchResult> rankMatches(Profile source, MatchingMode mode) {
		List<Profile> rawCandidates = findCandidatePool(source);
		List<Profile> candidates = source.search(rawCandidates);
		MatchingStrategy strategy = strategies.getOrDefault(mode, strategies.get(MatchingMode.RECOMMENDATION));
		LikeSnapshot snapshot = profileLikeService.getSnapshot(source.getUser().getId());
		MatchingContext context = new MatchingContext(snapshot.likedUserIds(), snapshot.likedByUserIds());

		return strategy.rank(source, candidates, context).stream()
			.map(scoredProfile -> toMatchResult(source, scoredProfile, snapshot))
			.toList();
	}

	private List<Profile> findCandidatePool(Profile source) {
		LocalDateTime activeAfter = LocalDateTime.now().minusDays(7);

		if (source.getInterests() == null || source.getInterests().isEmpty()) {
			return List.of();
		}

		return profileRepository.findVisibleCandidatesBySharedInterests(
			source.getUser().getId(),
			ProfileStateType.GHOST_MODE,
			ProfileStateType.LIGHT_TALK,
			activeAfter,
			source.getInterests(),
			MINIMUM_SHARED_INTERESTS
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
			buildWhyMatched(source, candidate, scoredProfile.score(), normalizedDistance, likedByYou, likedYou, mutualLike),
			likedByYou,
			likedYou,
			mutualLike
		);
	}

	private List<String> buildWhyMatched(
		Profile source,
		Profile candidate,
		MatchScore score,
		Double distanceKm,
		boolean likedByYou,
		boolean likedYou,
		boolean mutualLike
	) {
		List<String> reasons = new ArrayList<>();
		int sharedInterestCount = source.sharedInterests(candidate).size();
		if (sharedInterestCount > 0) {
			reasons.add(sharedInterestCount == 1
				? "Есть общий интерес"
				: "Есть %s общих интереса".formatted(sharedInterestCount));
		}
		if (distanceKm != null && score.distance() >= 55) {
			reasons.add(distanceKm < 5
				? "Находитесь совсем рядом"
				: "Находитесь недалеко друг от друга");
		}
		if (score.intention() >= 80) {
			reasons.add("Совпадают ожидания от знакомства");
		}
		if (score.activity() >= 65) {
			reasons.add("Пользователь недавно был активен");
		}
		if (mutualLike) {
			reasons.add("У вас уже есть взаимная симпатия");
		} else if (likedYou) {
			reasons.add("Этот человек уже отметил тебя");
		} else if (likedByYou) {
			reasons.add("Ты уже отправила симпатию");
		}
		if (reasons.isEmpty()) {
			reasons.add("Совпадение найдено по общей формуле совместимости");
		}
		return reasons.stream().limit(4).toList();
	}

	private Double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
}
