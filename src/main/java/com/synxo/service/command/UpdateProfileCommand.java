package com.synxo.service.command;

import java.util.Set;

public record UpdateProfileCommand(
	Integer age,
	String bio,
	String city,
	Double latitude,
	Double longitude,
	Set<String> interests
) {
}
