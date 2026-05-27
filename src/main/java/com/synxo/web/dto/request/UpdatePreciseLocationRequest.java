package com.synxo.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdatePreciseLocationRequest(
	@NotNull(message = "Location mode is required")
	Boolean enabled,
	Double latitude,
	Double longitude
) {
}
