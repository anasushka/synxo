package com.synxo.repository;

import com.synxo.domain.model.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	@Query("""
		select m from ChatMessage m
		where (m.sender.id = :userId and m.recipient.id = :otherUserId)
		   or (m.sender.id = :otherUserId and m.recipient.id = :userId)
		order by m.createdAt asc, m.id asc
		""")
	List<ChatMessage> findConversation(
		@Param("userId") Long userId,
		@Param("otherUserId") Long otherUserId
	);

	@Query("""
		select m from ChatMessage m
		where m.sender.id = :userId or m.recipient.id = :userId
		order by m.createdAt desc, m.id desc
		""")
	List<ChatMessage> findInboxMessages(@Param("userId") Long userId);
}
