package com.synxo.service.model;

import com.synxo.domain.enums.ProfileStateType;
import java.util.List;
import java.util.Set;

public record MatchResult(
	Long profileId,
	Long userId,
	String displayName,
	String photoUrl,
	Integer age,
	String city,
	ProfileStateType state,
	Set<String> sharedInterests,
	Double distanceKm,
	Double score,
	Double interestScore,
	Double distanceScore,
	Double intentionScore,
	Double activityScore,
	Double socialScore,
	List<String> whyMatched,
	boolean likedByYou,
	boolean likedYou,
	boolean mutualLike
) {
}
