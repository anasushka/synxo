package com.synxo.service.impl;

import com.synxo.domain.exception.ConflictException;
import com.synxo.domain.exception.ResourceNotFoundException;
import com.synxo.domain.model.ChatMessage;
import com.synxo.domain.model.Profile;
import com.synxo.domain.model.User;
import com.synxo.repository.ChatMessageRepository;
import com.synxo.repository.ProfileRepository;
import com.synxo.repository.UserRepository;
import com.synxo.service.ChatService;
import com.synxo.service.NotificationService;
import com.synxo.service.ProfileLikeService;
import com.synxo.service.command.SendMessageCommand;
import com.synxo.service.model.ChatMessageView;
import com.synxo.service.model.ChatPreview;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

	private final ChatMessageRepository chatMessageRepository;
	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;
	private final NotificationService notificationService;
	private final ProfileLikeService profileLikeService;

	@Override
	@Transactional(readOnly = true)
	public List<ChatPreview> getInbox(String email) {
		User currentUser = getUserByEmail(email);
		Set<Long> mutualUserIds = profileLikeService.findMutualUserIds(currentUser.getId());
		if (mutualUserIds.isEmpty()) {
			return List.of();
		}

		List<ChatMessage> messages = chatMessageRepository.findInboxMessages(currentUser.getId());
		Map<Long, ChatMessage> latestMessages = new HashMap<>();

		for (ChatMessage message : messages) {
			User counterpart = message.getSender().getId().equals(currentUser.getId()) ? message.getRecipient() : message.getSender();
			if (mutualUserIds.contains(counterpart.getId()) && !latestMessages.containsKey(counterpart.getId())) {
				latestMessages.put(counterpart.getId(), message);
			}
		}

		List<ChatPreview> previews = new ArrayList<>();
		for (User counterpart : userRepository.findByIdIn(mutualUserIds)) {
			ChatMessage latestMessage = latestMessages.get(counterpart.getId());
			previews.add(toChatPreview(currentUser, counterpart, latestMessage));
		}

		previews.sort(Comparator
			.comparing(ChatPreview::lastMessageAt, Comparator.nullsLast(Comparator.reverseOrder()))
			.thenComparing(ChatPreview::displayName, String.CASE_INSENSITIVE_ORDER));

		return previews;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ChatMessageView> getConversation(String email, Long otherUserId) {
		User currentUser = getUserByEmail(email);
		User otherUser = userRepository.findById(otherUserId)
			.orElseThrow(() -> new ResourceNotFoundException("Recipient with id %s not found".formatted(otherUserId)));
		ensureMutualLike(currentUser, otherUser);

		return chatMessageRepository.findConversation(currentUser.getId(), otherUser.getId()).stream()
			.map(message -> toMessageView(currentUser, message))
			.toList();
	}

	@Override
	public ChatMessageView sendMessage(String email, SendMessageCommand command) {
		User sender = getUserByEmail(email);
		User recipient = userRepository.findById(command.recipientUserId())
			.orElseThrow(() -> new ResourceNotFoundException("Recipient with id %s not found".formatted(command.recipientUserId())));
		ensureMutualLike(sender, recipient);

		ChatMessage savedMessage = chatMessageRepository.save(ChatMessage.builder()
			.sender(sender)
			.recipient(recipient)
			.content(command.content().trim())
			.build());

		markProfileActive(sender.getId());
		notificationService.createMessageNotification(recipient.getId(), sender.getId(), savedMessage.getContent());

		return toMessageView(sender, savedMessage);
	}

	private User getUserByEmail(String email) {
		return userRepository.findByEmail(normalizeEmail(email))
			.orElseThrow(() -> new ResourceNotFoundException("User with email %s not found".formatted(email)));
	}

	private ChatMessageView toMessageView(User currentUser, ChatMessage message) {
		return new ChatMessageView(
			message.getId(),
			message.getSender().getId(),
			message.getRecipient().getId(),
			message.getContent(),
			message.getCreatedAt(),
			message.getSender().getId().equals(currentUser.getId())
		);
	}

	private ChatPreview toChatPreview(User currentUser, User counterpart, ChatMessage latestMessage) {
		if (latestMessage == null) {
			return new ChatPreview(
				counterpart.getId(),
				counterpart.getDisplayName(),
				counterpart.getProfile() == null ? null : counterpart.getProfile().getPhotoUrl(),
				"Взаимная симпатия. Можно начать диалог.",
				null,
				false
			);
		}

		return new ChatPreview(
			counterpart.getId(),
			counterpart.getDisplayName(),
			counterpart.getProfile() == null ? null : counterpart.getProfile().getPhotoUrl(),
			latestMessage.getContent(),
			latestMessage.getCreatedAt(),
			latestMessage.getSender().getId().equals(currentUser.getId())
		);
	}

	private void markProfileActive(Long userId) {
		profileRepository.findByUserId(userId)
			.ifPresent(Profile::markActive);
	}

	private void ensureMutualLike(User currentUser, User otherUser) {
		if (!profileLikeService.isMutualLike(currentUser.getId(), otherUser.getId())) {
			throw new ConflictException("Messaging is available only after a mutual like.");
		}
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
