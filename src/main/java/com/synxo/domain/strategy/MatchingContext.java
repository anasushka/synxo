package com.synxo.domain.strategy;

import com.synxo.domain.model.Profile;
import java.util.Set;

public record MatchingContext(
	Set<Long> likedUserIds,
	Set<Long> likedByUserIds
) {

	public static MatchingContext empty() {
		return new MatchingContext(Set.of(), Set.of());
	}

	public boolean likedByYou(Profile profile) {
		Long userId = resolveUserId(profile);
		return userId != null && likedUserIds.contains(userId);
	}

	public boolean likedYou(Profile profile) {
		Long userId = resolveUserId(profile);
		return userId != null && likedByUserIds.contains(userId);
	}

	public boolean mutualLike(Profile profile) {
		return likedByYou(profile) && likedYou(profile);
	}

	private Long resolveUserId(Profile profile) {
		if (profile == null) {
			return null;
		}
		if (profile.getUser() != null) {
			return profile.getUser().getId();
		}
		return profile.getId();
	}
}
