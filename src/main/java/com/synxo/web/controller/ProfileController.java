package com.synxo.web.controller;

import com.synxo.domain.model.Profile;
import com.synxo.service.ProfileService;
import com.synxo.web.dto.request.UpdateProfileRequest;
import com.synxo.web.dto.request.UpdateProfileStateRequest;
import com.synxo.web.dto.response.ProfileResponse;
import com.synxo.web.mapper.ApiMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

	private final ProfileService profileService;
	private final ApiMapper apiMapper;

	@GetMapping("/me")
	public ProfileResponse currentProfile(Authentication authentication) {
		return apiMapper.toProfileResponse(profileService.getCurrentProfile(authentication.getName()));
	}

	@PutMapping("/me")
	public ProfileResponse updateProfile(
		Authentication authentication,
		@Valid @RequestBody UpdateProfileRequest request
	) {
		Profile profile = profileService.updateProfile(authentication.getName(), apiMapper.toCommand(request));
		return apiMapper.toProfileResponse(profile);
	}

	@PatchMapping("/me/state")
	public ProfileResponse changeState(
		Authentication authentication,
		@Valid @RequestBody UpdateProfileStateRequest request
	) {
		Profile profile = profileService.changeState(authentication.getName(), request.state());
		return apiMapper.toProfileResponse(profile);
	}

	@PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ProfileResponse uploadPhoto(
		Authentication authentication,
		@RequestParam("file") MultipartFile file
	) {
		Profile profile = profileService.updatePhoto(authentication.getName(), file);
		return apiMapper.toProfileResponse(profile);
	}
}
