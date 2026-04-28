package com.synxo.infrastructure.security;

import com.synxo.domain.model.User;
import com.synxo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DatabaseUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) {
		User user = userRepository.findByEmail(username.trim().toLowerCase())
			.orElseThrow(() -> new UsernameNotFoundException("User with email %s not found".formatted(username)));

		return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
			.password(user.getPassword())
			.roles(user.getRole().name())
			.build();
	}
}
