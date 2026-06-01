package com.synxo.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateMatchingPreferencesRequest(
	@NotNull(message = "Enabled flag is required")
	Boolean enabled,

	@NotNull(message = "Interest priority is required")
	@Min(value = 0, message = "Priority must be between 0 and 100")
	@Max(value = 100, message = "Priority must be between 0 and 100")
	Integer interestPriority,

	@NotNull(message = "Distance priority is required")
	@Min(value = 0, message = "Priority must be between 0 and 100")
	@Max(value = 100, message = "Priority must be between 0 and 100")
	Integer distancePriority,

	@NotNull(message = "Intention priority is required")
	@Min(value = 0, message = "Priority must be between 0 and 100")
	@Max(value = 100, message = "Priority must be between 0 and 100")
	Integer intentionPriority,

	@NotNull(message = "Activity priority is required")
	@Min(value = 0, message = "Priority must be between 0 and 100")
	@Max(value = 100, message = "Priority must be between 0 and 100")
	Integer activityPriority,

	@NotNull(message = "Social priority is required")
	@Min(value = 0, message = "Priority must be between 0 and 100")
	@Max(value = 100, message = "Priority must be between 0 and 100")
	Integer socialPriority
) {
}
