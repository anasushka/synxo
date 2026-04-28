package com.synxo.web.dto.request;

import com.synxo.domain.enums.ProfileStateType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record RegisterRequest(
	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	String email,

	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Password must contain at least 8 characters")
	String password,

	@NotBlank(message = "Display name is required")
	String displayName,

	@NotNull(message = "Age is required")
	@Min(value = 18, message = "You must be at least 18 years old")
	Integer age,

	@Size(max = 500, message = "Bio must be shorter than 500 characters")
	String bio,

	@NotBlank(message = "City is required")
	String city,

	@NotNull(message = "Latitude is required")
	Double latitude,

	@NotNull(message = "Longitude is required")
	Double longitude,

	@NotEmpty(message = "At least one interest is required")
	Set<@NotBlank(message = "Interest cannot be blank") String> interests,

	@NotNull(message = "Profile state is required")
	ProfileStateType state
) {
}
