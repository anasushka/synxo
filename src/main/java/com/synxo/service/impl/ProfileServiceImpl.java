package com.synxo.service.impl;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.exception.ResourceNotFoundException;
import com.synxo.domain.model.Profile;
import com.synxo.repository.ProfileRepository;
import com.synxo.service.NotificationService;
import com.synxo.service.ProfileImageStorageService;
import com.synxo.service.ProfileService;
import com.synxo.service.command.UpdateProfileCommand;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileServiceImpl implements ProfileService {

	private final ProfileRepository profileRepository;
	private final NotificationService notificationService;
	private final ProfileImageStorageService profileImageStorageService;

	@Override
	@Transactional(readOnly = true)
	public Profile getCurrentProfile(String email) {
		return profileRepository.findByUserEmail(normalizeEmail(email))
			.orElseThrow(() -> new ResourceNotFoundException("Profile for %s not found".formatted(email)));
	}

	@Override
	public Profile changeState(String email, ProfileStateType state) {
		Profile profile = getProfileByEmail(email);

		profile.setState(state);
		profile.markActive();
		Profile updatedProfile = profileRepository.save(profile);
		notificationService.createStateChangedNotification(updatedProfile.getUser().getId(), state);
		return updatedProfile;
	}

	@Override
	public Profile updateProfile(String email, UpdateProfileCommand command) {
		Profile profile = getProfileByEmail(email);
		profile.getUser().setAge(command.age());
		profile.setBio(command.bio());
		profile.setCity(command.city().trim());
		profile.setLatitude(command.latitude());
		profile.setLongitude(command.longitude());
		profile.setInterests(normalizeInterests(command.interests()));
		profile.markActive();
		return profileRepository.save(profile);
	}

	@Override
	public Profile updatePhoto(String email, MultipartFile file) {
		Profile profile = getProfileByEmail(email);
		String photoUrl = profileImageStorageService.store(profile.getId(), file, profile.getPhotoUrl());
		profile.setPhotoUrl(photoUrl);
		profile.markActive();
		return profileRepository.save(profile);
	}

	private Profile getProfileByEmail(String email) {
		return profileRepository.findByUserEmail(normalizeEmail(email))
			.orElseThrow(() -> new ResourceNotFoundException("Profile for %s not found".formatted(email)));
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private Set<String> normalizeInterests(Set<String> interests) {
		if (interests == null) {
			return Set.of();
		}

		return interests.stream()
			.map(String::trim)
			.filter(value -> !value.isBlank())
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
