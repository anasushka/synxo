package com.synxo.web.controller;

import com.synxo.service.AuthService;
import com.synxo.domain.model.User;
import com.synxo.web.dto.request.RegisterRequest;
import com.synxo.web.dto.response.UserResponse;
import com.synxo.web.mapper.ApiMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final ApiMapper apiMapper;

	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		User user = authService.register(apiMapper.toCommand(request));
		return ResponseEntity.status(HttpStatus.CREATED).body(apiMapper.toUserResponse(user));
	}

	@GetMapping("/me")
	public UserResponse currentUser(Authentication authentication) {
		return apiMapper.toUserResponse(authService.getCurrentUser(authentication.getName()));
	}
}
