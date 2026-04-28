package com.synxo.service.impl;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.exception.ConflictException;
import com.synxo.domain.exception.ResourceNotFoundException;
import com.synxo.domain.model.Profile;
import com.synxo.domain.model.User;
import com.synxo.repository.UserRepository;
import com.synxo.service.AuthService;
import com.synxo.service.NotificationService;
import com.synxo.service.command.RegisterUserCommand;
import com.synxo.service.util.ServiceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final NotificationService notificationService;

	@Override
	public User register(RegisterUserCommand command) {
		String email = ServiceUtils.normalizeEmail(command.email());
		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("User with email %s already exists".formatted(email));
		}

		Profile profile = Profile.builder()
			.bio(command.bio())
			.city(command.city().trim())
			.latitude(command.latitude())
			.longitude(command.longitude())
			.interests(ServiceUtils.normalizeInterests(command.interests()))
			.state(command.state() == null ? ProfileStateType.DEEP_SEARCH : command.state())
			.build();

		User user = User.builder()
			.email(email)
			.password(passwordEncoder.encode(command.password()))
			.displayName(command.displayName().trim())
			.age(command.age())
			.build();
		user.attachProfile(profile);

		User savedUser = userRepository.save(user);
		notificationService.createWelcomeNotification(savedUser.getId());
		return savedUser;
	}

	@Override
	@Transactional(readOnly = true)
	public User getCurrentUser(String email) {
		return userRepository.findByEmail(ServiceUtils.normalizeEmail(email))
			.orElseThrow(() -> new ResourceNotFoundException("User with email %s not found".formatted(email)));
	}
}
