package com.synxo.service.impl;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.exception.ResourceNotFoundException;
import com.synxo.domain.model.Profile;
import com.synxo.repository.ProfileRepository;
import com.synxo.service.AchievementService;
import com.synxo.service.LocationService;
import com.synxo.service.NotificationService;
import com.synxo.service.ProfileImageStorageService;
import com.synxo.service.ProfileService;
import com.synxo.service.command.UpdateMatchingPreferencesCommand;
import com.synxo.service.command.UpdatePreciseLocationCommand;
import com.synxo.service.command.UpdateProfileCommand;
import com.synxo.service.model.Coordinates;
import com.synxo.service.util.ServiceUtils;
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
	private final LocationService locationService;
	private final AchievementService achievementService;

	@Override
	@Transactional(readOnly = true)
	public Profile getCurrentProfile(String email) {
		return profileRepository.findByUserEmail(ServiceUtils.normalizeEmail(email))
			.orElseThrow(() -> new ResourceNotFoundException("Profile for %s not found".formatted(email)));
	}

	@Override
	public Profile changeState(String email, ProfileStateType state) {
		Profile profile = getProfileByEmail(email);

		profile.setState(state);
		profile.markActive();
		Profile updatedProfile = profileRepository.save(profile);
		notificationService.createStateChangedNotification(updatedProfile.getUser().getId(), state);
		achievementService.evaluateForUser(updatedProfile.getUser().getId());
		return updatedProfile;
	}

	@Override
	public Profile updateProfile(String email, UpdateProfileCommand command) {
		Profile profile = getProfileByEmail(email);
		String city = command.city().trim();

		profile.getUser().setAge(command.age());
		profile.setBio(command.bio());
		updateCityCoordinatesIfNeeded(profile, city);
		profile.setInterests(ServiceUtils.normalizeInterests(command.interests()));
		profile.markActive();
		Profile updatedProfile = profileRepository.save(profile);
		achievementService.evaluateForUser(updatedProfile.getUser().getId());
		return updatedProfile;
	}

	@Override
	public Profile updateMatchingPreferences(String email, UpdateMatchingPreferencesCommand command) {
		Profile profile = getProfileByEmail(email);

		profile.setMatchingPreferencesEnabled(command.enabled());
		profile.setInterestPriority(command.interestPriority());
		profile.setDistancePriority(command.distancePriority());
		profile.setIntentionPriority(command.intentionPriority());
		profile.setActivityPriority(command.activityPriority());
		profile.setSocialPriority(command.socialPriority());
		profile.markActive();

		Profile updatedProfile = profileRepository.save(profile);
		achievementService.evaluateForUser(updatedProfile.getUser().getId());
		return updatedProfile;
	}

	@Override
	public Profile updatePreciseLocation(String email, UpdatePreciseLocationCommand command) {
		Profile profile = getProfileByEmail(email);

		if (!command.enabled()) {
			profile.setPreciseLocationEnabled(false);
			profile.setPreciseLatitude(null);
			profile.setPreciseLongitude(null);
			profile.markActive();
			Profile updatedProfile = profileRepository.save(profile);
			achievementService.evaluateForUser(updatedProfile.getUser().getId());
			return updatedProfile;
		}

		Coordinates preciseCoordinates = locationService.preciseCoordinates(command.latitude(), command.longitude());
		profile.setPreciseLatitude(preciseCoordinates.latitude());
		profile.setPreciseLongitude(preciseCoordinates.longitude());
		profile.setPreciseLocationEnabled(true);
		profile.markActive();
		Profile updatedProfile = profileRepository.save(profile);
		achievementService.evaluateForUser(updatedProfile.getUser().getId());
		return updatedProfile;
	}

	@Override
	public Profile updatePhoto(String email, MultipartFile file) {
		Profile profile = getProfileByEmail(email);
		String photoUrl = profileImageStorageService.store(profile.getId(), file, profile.getPhotoUrl());
		profile.setPhotoUrl(photoUrl);
		profile.markActive();
		Profile updatedProfile = profileRepository.save(profile);
		achievementService.evaluateForUser(updatedProfile.getUser().getId());
		return updatedProfile;
	}

	private Profile getProfileByEmail(String email) {
		return profileRepository.findByUserEmail(ServiceUtils.normalizeEmail(email))
			.orElseThrow(() -> new ResourceNotFoundException("Profile for %s not found".formatted(email)));
	}

	private void updateCityCoordinatesIfNeeded(Profile profile, String city) {
		boolean cityChanged = profile.getCity() == null || !profile.getCity().equalsIgnoreCase(city);
		if (!cityChanged && profile.getLatitude() != null && profile.getLongitude() != null) {
			return;
		}

		Coordinates cityCoordinates = locationService.resolveCity(city);
		profile.setCity(city);
		profile.setLatitude(cityCoordinates.latitude());
		profile.setLongitude(cityCoordinates.longitude());
	}
}
