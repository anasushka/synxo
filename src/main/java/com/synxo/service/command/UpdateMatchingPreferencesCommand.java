package com.synxo.service.command;

public record UpdateMatchingPreferencesCommand(
	boolean enabled,
	Integer interestPriority,
	Integer distancePriority,
	Integer intentionPriority,
	Integer activityPriority,
	Integer socialPriority
) {
}
