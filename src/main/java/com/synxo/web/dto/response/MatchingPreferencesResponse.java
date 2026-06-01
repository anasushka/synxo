package com.synxo.web.dto.response;

public record MatchingPreferencesResponse(
	boolean enabled,
	Integer interestPriority,
	Integer distancePriority,
	Integer intentionPriority,
	Integer activityPriority,
	Integer socialPriority
) {
}
