package com.synxo.service;

import com.synxo.service.command.SendMessageCommand;
import com.synxo.service.model.ChatMessageView;
import com.synxo.service.model.ChatPreview;
import java.util.List;

public interface ChatService {

	List<ChatPreview> getInbox(String email);

	List<ChatMessageView> getConversation(String email, Long otherUserId);

	ChatMessageView sendMessage(String email, SendMessageCommand command);
}
