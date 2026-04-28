package com.synxo.service.model;

import java.util.Set;

public record LikeSnapshot(
	Set<Long> likedUserIds,
	Set<Long> likedByUserIds
) {

	public boolean likedByYou(Long userId) {
		return likedUserIds.contains(userId);
	}

	public boolean likedYou(Long userId) {
		return likedByUserIds.contains(userId);
	}

	public boolean mutualLike(Long userId) {
		return likedByYou(userId) && likedYou(userId);
	}
}
