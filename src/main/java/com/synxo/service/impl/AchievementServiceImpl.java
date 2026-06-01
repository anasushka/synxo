package com.synxo.service.impl;

import com.synxo.domain.enums.AchievementType;
import com.synxo.domain.model.Profile;
import com.synxo.domain.model.User;
import com.synxo.domain.model.UserAchievement;
import com.synxo.repository.ChatMessageRepository;
import com.synxo.repository.ProfileLikeRepository;
import com.synxo.repository.ProfileRepository;
import com.synxo.repository.UserAchievementRepository;
import com.synxo.repository.UserRepository;
import com.synxo.service.AchievementService;
import com.synxo.service.NotificationService;
import com.synxo.service.model.AchievementView;
import com.synxo.service.util.ServiceUtils;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AchievementServiceImpl implements AchievementService {

	private static final int COMPLETE_PROFILE_MIN_INTERESTS = 5;

	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;
	private final ProfileLikeRepository profileLikeRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final UserAchievementRepository userAchievementRepository;
	private final NotificationService notificationService;

	@Override
	public void evaluateForUser(Long userId) {
		profileRepository.findByUserId(userId).ifPresent(profile -> {
			List<AchievementType> unlocked = new ArrayList<>();

			maybeUnlock(userId, AchievementType.FIRST_MATCH, profileLikeRepository.countMutualMatches(userId) >= 1, unlocked);
			maybeUnlock(userId, AchievementType.TEN_MUTUAL_LIKES, profileLikeRepository.countMutualMatches(userId) >= 10, unlocked);
			maybeUnlock(userId, AchievementType.FIRST_DIALOG, chatMessageRepository.countBySenderId(userId) >= 1, unlocked);
			maybeUnlock(userId, AchievementType.PROFILE_COMPLETE, isProfileComplete(profile), unlocked);
			maybeUnlock(userId, AchievementType.ACTIVE_WEEK, normalizeStreak(profile) >= 7, unlocked);

			unlocked.forEach(type -> notificationService.createSystemNotification(userId, type.notificationMessage()));
		});
	}

	@Override
	@Transactional(readOnly = true)
	public List<AchievementView> getAchievements(String email) {
		User user = userRepository.findByEmail(ServiceUtils.normalizeEmail(email))
			.orElseThrow(() -> new IllegalArgumentException("User with email %s not found".formatted(email)));

		return userAchievementRepository.findByUserIdOrderByUnlockedAtAscIdAsc(user.getId()).stream()
			.map(achievement -> new AchievementView(
				achievement.getType().name(),
				achievement.getType().title(),
				achievement.getType().description(),
				achievement.getUnlockedAt()
			))
			.toList();
	}

	private void maybeUnlock(Long userId, AchievementType type, boolean condition, List<AchievementType> unlocked) {
		if (!condition || userAchievementRepository.existsByUserIdAndType(userId, type)) {
			return;
		}

		userAchievementRepository.save(UserAchievement.builder()
			.userId(userId)
			.type(type)
			.build());
		unlocked.add(type);
	}

	private boolean isProfileComplete(Profile profile) {
		return profile.getPhotoUrl() != null
			&& !profile.getPhotoUrl().isBlank()
			&& profile.getBio() != null
			&& !profile.getBio().isBlank()
			&& profile.getCity() != null
			&& !profile.getCity().isBlank()
			&& profile.getInterests() != null
			&& profile.getInterests().size() >= COMPLETE_PROFILE_MIN_INTERESTS;
	}

	private int normalizeStreak(Profile profile) {
		Integer streak = profile.getActivityStreakDays();
		return streak == null ? 0 : Math.max(0, streak);
	}
}
