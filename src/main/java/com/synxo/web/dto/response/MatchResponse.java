package com.synxo.web.dto.response;

import com.synxo.domain.enums.ProfileStateType;
import java.util.Set;

public record MatchResponse(
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
	boolean likedByYou,
	boolean likedYou,
	boolean mutualLike
) {
}
