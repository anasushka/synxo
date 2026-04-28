package com.synxo.web.controller;

import com.synxo.service.ChatService;
import com.synxo.web.dto.request.SendMessageRequest;
import com.synxo.web.dto.response.ChatMessageResponse;
import com.synxo.web.dto.response.ChatPreviewResponse;
import com.synxo.web.mapper.ApiMapper;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;
	private final ApiMapper apiMapper;

	@GetMapping
	public List<ChatPreviewResponse> inbox(Authentication authentication) {
		return chatService.getInbox(authentication.getName()).stream()
			.map(apiMapper::toChatPreviewResponse)
			.toList();
	}

	@GetMapping("/{otherUserId}/messages")
	public List<ChatMessageResponse> conversation(
		Authentication authentication,
		@PathVariable Long otherUserId
	) {
		return chatService.getConversation(authentication.getName(), otherUserId).stream()
			.map(apiMapper::toChatMessageResponse)
			.toList();
	}

	@PostMapping("/messages")
	public ChatMessageResponse sendMessage(
		Authentication authentication,
		@Valid @RequestBody SendMessageRequest request
	) {
		return apiMapper.toChatMessageResponse(
			chatService.sendMessage(authentication.getName(), apiMapper.toCommand(request))
		);
	}
}
