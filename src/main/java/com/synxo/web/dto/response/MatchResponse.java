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
	boolean likedByYou,
	boolean likedYou,
	boolean mutualLike
) {
}
