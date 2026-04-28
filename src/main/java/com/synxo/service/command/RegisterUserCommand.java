package com.synxo.service.command;

import com.synxo.domain.enums.ProfileStateType;
import java.util.Set;

public record RegisterUserCommand(
	String email,
	String password,
	String displayName,
	Integer age,
	String bio,
	String city,
	Double latitude,
	Double longitude,
	Set<String> interests,
	ProfileStateType state
) {
}
