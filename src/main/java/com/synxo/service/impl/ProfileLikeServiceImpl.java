package com.synxo.service.impl;

import com.synxo.domain.model.ProfileLike;
import com.synxo.domain.model.User;
import com.synxo.repository.ProfileLikeRepository;
import com.synxo.repository.UserRepository;
import com.synxo.service.NotificationService;
import com.synxo.service.ProfileLikeService;
import com.synxo.service.model.LikeResult;
import com.synxo.service.model.LikeSnapshot;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfileLikeServiceImpl implements ProfileLikeService {

	private final ProfileLikeRepository profileLikeRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;

	public ProfileLikeServiceImpl(
		ProfileLikeRepository profileLikeRepository,
		UserRepository userRepository,
		NotificationService notificationService
	) {
		this.profileLikeRepository = profileLikeRepository;
		this.userRepository = userRepository;
		this.notificationService = notificationService;
	}

	@Override
	@Transactional(readOnly = true)
	public LikeSnapshot getSnapshot(Long userId) {
		return new LikeSnapshot(
			new LinkedHashSet<>(profileLikeRepository.findLikedUserIds(userId)),
			new LinkedHashSet<>(profileLikeRepository.findLikedByUserIds(userId))
		);
	}

	@Override
	public LikeResult like(Long likerUserId, Long likedUserId) {
		if (likerUserId.equals(likedUserId)) {
			throw new IllegalArgumentException("You cannot like your own profile.");
		}

		boolean alreadyLiked = profileLikeRepository.existsByLikerIdAndLikedId(likerUserId, likedUserId);
		boolean likedYou = profileLikeRepository.existsByLikerIdAndLikedId(likedUserId, likerUserId);

		if (!alreadyLiked) {
			User liker = userRepository.getReferenceById(likerUserId);
			User liked = userRepository.getReferenceById(likedUserId);
			profileLikeRepository.save(ProfileLike.builder()
				.liker(liker)
				.liked(liked)
				.build());
		}

		boolean mutualLike = likedYou;
		if (!alreadyLiked && mutualLike) {
			notificationService.createMatchNotification(likerUserId, likedUserId);
			notificationService.createMatchNotification(likedUserId, likerUserId);
		}

		return new LikeResult(true, likedYou, mutualLike, !alreadyLiked);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isMutualLike(Long firstUserId, Long secondUserId) {
		return profileLikeRepository.existsByLikerIdAndLikedId(firstUserId, secondUserId)
			&& profileLikeRepository.existsByLikerIdAndLikedId(secondUserId, firstUserId);
	}

	@Override
	@Transactional(readOnly = true)
	public Set<Long> findMutualUserIds(Long userId) {
		LikeSnapshot snapshot = getSnapshot(userId);
		Set<Long> mutualUserIds = new LinkedHashSet<>(snapshot.likedUserIds());
		mutualUserIds.retainAll(snapshot.likedByUserIds());
		return mutualUserIds;
	}
}
