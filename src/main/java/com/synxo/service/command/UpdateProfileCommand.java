package com.synxo.service.command;

import java.util.Set;

public record UpdateProfileCommand(
	Integer age,
	String bio,
	String city,
	Set<String> interests
) {
}
