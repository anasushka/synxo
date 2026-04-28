package com.synxo.service.command;

public record SendMessageCommand(
	Long recipientUserId,
	String content
) {
}
