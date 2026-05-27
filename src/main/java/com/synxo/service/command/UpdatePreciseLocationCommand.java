package com.synxo.service.command;

public record UpdatePreciseLocationCommand(
	boolean enabled,
	Double latitude,
	Double longitude
) {
}
