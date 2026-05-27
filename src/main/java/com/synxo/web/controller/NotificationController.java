package com.synxo.web.controller;

import com.synxo.service.NotificationService;
import com.synxo.web.dto.response.NotificationResponse;
import com.synxo.web.mapper.ApiMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;
	private final ApiMapper apiMapper;

	@GetMapping
	public List<NotificationResponse> unread(Authentication authentication) {
		return notificationService.getUnreadNotifications(authentication.getName()).stream()
			.map(apiMapper::toNotificationResponse)
			.toList();
	}

	@PatchMapping("/{notificationId}/dismiss")
	public void dismiss(Authentication authentication, @PathVariable Long notificationId) {
		notificationService.dismissNotification(authentication.getName(), notificationId);
	}
}
