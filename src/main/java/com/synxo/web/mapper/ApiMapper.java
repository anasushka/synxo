package com.synxo.web.mapper;

import com.synxo.domain.model.Profile;
import com.synxo.domain.model.User;
import com.synxo.domain.model.UserNotification;
import com.synxo.service.command.SendMessageCommand;
import com.synxo.service.command.RegisterUserCommand;
import com.synxo.service.command.UpdatePreciseLocationCommand;
import com.synxo.service.command.UpdateProfileCommand;
import com.synxo.service.model.ChatMessageView;
import com.synxo.service.model.ChatPreview;
import com.synxo.service.model.MatchResult;
import com.synxo.web.dto.request.RegisterRequest;
import com.synxo.web.dto.request.SendMessageRequest;
import com.synxo.web.dto.request.UpdatePreciseLocationRequest;
import com.synxo.web.dto.request.UpdateProfileRequest;
import com.synxo.web.dto.response.ChatMessageResponse;
import com.synxo.web.dto.response.ChatPreviewResponse;
import com.synxo.web.dto.response.MatchResponse;
import com.synxo.web.dto.response.NotificationResponse;
import com.synxo.web.dto.response.ProfileResponse;
import com.synxo.web.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class ApiMapper {

	public RegisterUserCommand toCommand(RegisterRequest request) {
		return new RegisterUserCommand(
			request.email(),
			request.password(),
			request.displayName(),
			request.age(),
			request.bio(),
			request.city(),
			request.interests(),
			request.state()
		);
	}

	public UserResponse toUserResponse(User user) {
		return new UserResponse(
			user.getId(),
			user.getEmail(),
			user.getDisplayName(),
			user.getAge(),
			user.getRole().name(),
			user.getCreatedAt()
		);
	}

	public ProfileResponse toProfileResponse(Profile profile) {
		return new ProfileResponse(
			profile.getId(),
			profile.getUser().getId(),
			profile.getUser().getDisplayName(),
			profile.getPhotoUrl(),
			profile.getUser().getAge(),
			profile.getCity(),
			profile.getBio(),
			profile.effectiveLatitude(),
			profile.effectiveLongitude(),
			profile.getLatitude(),
			profile.getLongitude(),
			profile.getPreciseLatitude(),
			profile.getPreciseLongitude(),
			profile.hasPreciseLocation(),
			profile.getState(),
			profile.getInterests(),
			profile.getLastActiveAt()
		);
	}

	public MatchResponse toMatchResponse(MatchResult matchResult) {
		return new MatchResponse(
			matchResult.profileId(),
			matchResult.userId(),
			matchResult.displayName(),
			matchResult.photoUrl(),
			matchResult.age(),
			matchResult.city(),
			matchResult.state(),
			matchResult.sharedInterests(),
			matchResult.distanceKm(),
			matchResult.score(),
			matchResult.interestScore(),
			matchResult.distanceScore(),
			matchResult.intentionScore(),
			matchResult.activityScore(),
			matchResult.socialScore(),
			matchResult.likedByYou(),
			matchResult.likedYou(),
			matchResult.mutualLike()
		);
	}

	public UpdateProfileCommand toCommand(UpdateProfileRequest request) {
		return new UpdateProfileCommand(
			request.age(),
			request.bio(),
			request.city(),
			request.interests()
		);
	}

	public UpdatePreciseLocationCommand toCommand(UpdatePreciseLocationRequest request) {
		return new UpdatePreciseLocationCommand(request.enabled(), request.latitude(), request.longitude());
	}

	public SendMessageCommand toCommand(SendMessageRequest request) {
		return new SendMessageCommand(request.recipientUserId(), request.content());
	}

	public ChatPreviewResponse toChatPreviewResponse(ChatPreview preview) {
		return new ChatPreviewResponse(
			preview.userId(),
			preview.displayName(),
			preview.photoUrl(),
			preview.lastMessage(),
			preview.lastMessageAt(),
			preview.outgoing()
		);
	}

	public ChatMessageResponse toChatMessageResponse(ChatMessageView message) {
		return new ChatMessageResponse(
			message.id(),
			message.senderUserId(),
			message.recipientUserId(),
			message.content(),
			message.createdAt(),
			message.outgoing()
		);
	}

	public NotificationResponse toNotificationResponse(UserNotification notification) {
		return new NotificationResponse(
			notification.getId(),
			notification.getType(),
			notification.getMessage(),
			notification.getCreatedAt()
		);
	}
}
