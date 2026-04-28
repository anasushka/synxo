package com.synxo.web.dto.request;

import com.synxo.domain.enums.ProfileStateType;
import jakarta.validation.constraints.NotNull;

public record UpdateProfileStateRequest(
	@NotNull(message = "Profile state is required")
	ProfileStateType state
) {
}
