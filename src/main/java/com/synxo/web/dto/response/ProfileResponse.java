package com.synxo.web.dto.response;

import com.synxo.domain.enums.ProfileStateType;
import java.time.LocalDateTime;
import java.util.Set;

public record ProfileResponse(
	Long profileId,
	Long userId,
	String displayName,
	String photoUrl,
	Integer age,
	String city,
	String bio,
	Double latitude,
	Double longitude,
	ProfileStateType state,
	Set<String> interests,
	LocalDateTime lastActiveAt
) {
}
