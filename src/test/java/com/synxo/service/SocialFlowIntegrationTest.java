package com.synxo.service;

import com.synxo.domain.enums.MatchingMode;
import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.exception.ConflictException;
import com.synxo.service.command.RegisterUserCommand;
import com.synxo.service.command.SendMessageCommand;
import com.synxo.service.model.ChatMessageView;
import com.synxo.service.model.ChatPreview;
import com.synxo.service.model.MatchResult;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SocialFlowIntegrationTest {

	@Autowired
	private AuthService authService;

	@Autowired
	private MatchingService matchingService;

	@Autowired
	private ChatService chatService;

	@Test
	void matchesRequireSharedInterestsAndChatRequiresMutualLike() {
		register(
			"alice@example.com",
			"Alice",
			Set.of("Кошки", "Кофе", "Чтение")
		);
		register(
			"bella@example.com",
			"Bella",
			Set.of("Кошки", "Кофе", "Путешествия")
		);
		register(
			"clara@example.com",
			"Clara",
			Set.of("Спорт", "Суши", "Электроника")
		);

		Long aliceId = authService.getCurrentUser("alice@example.com").getId();
		Long bellaId = authService.getCurrentUser("bella@example.com").getId();
		Long claraId = authService.getCurrentUser("clara@example.com").getId();

		List<MatchResult> matches = matchingService.findMatches("alice@example.com", MatchingMode.RECOMMENDATION, 0, 20);

		assertThat(matches).extracting(MatchResult::userId)
			.contains(bellaId)
			.doesNotContain(claraId);

		assertThatThrownBy(() -> chatService.sendMessage("alice@example.com", new SendMessageCommand(bellaId, "Привет")))
			.isInstanceOf(ConflictException.class);

		MatchResult firstLike = matchingService.likeProfile("alice@example.com", bellaId);
		assertThat(firstLike.likedByYou()).isTrue();
		assertThat(firstLike.mutualLike()).isFalse();

		assertThatThrownBy(() -> chatService.sendMessage("alice@example.com", new SendMessageCommand(bellaId, "Привет")))
			.isInstanceOf(ConflictException.class);

		MatchResult reciprocalLike = matchingService.likeProfile("bella@example.com", aliceId);
		assertThat(reciprocalLike.mutualLike()).isTrue();

		List<ChatPreview> inbox = chatService.getInbox("alice@example.com");
		assertThat(inbox).extracting(ChatPreview::userId).contains(bellaId);

		ChatMessageView message = chatService.sendMessage("alice@example.com", new SendMessageCommand(bellaId, "Привет"));
		assertThat(message.outgoing()).isTrue();
		assertThat(message.content()).isEqualTo("Привет");

		assertThat(chatService.getConversation("bella@example.com", aliceId))
			.extracting(ChatMessageView::content)
			.containsExactly("Привет");
	}

	@Test
	void feedIsSymmetricForVisibleProfilesWithSharedInterest() {
		register(
			"deep@example.com",
			"Deep",
			Set.of("Кошки", "Рок", "Новые друзья"),
			ProfileStateType.DEEP_SEARCH
		);
		register(
			"light@example.com",
			"Light",
			Set.of("Кошки", "Суши", "Новые друзья"),
			ProfileStateType.LIGHT_TALK
		);

		Long deepId = authService.getCurrentUser("deep@example.com").getId();
		Long lightId = authService.getCurrentUser("light@example.com").getId();

		assertThat(matchingService.findMatches("deep@example.com", MatchingMode.RECOMMENDATION, 0, 20))
			.extracting(MatchResult::userId)
			.contains(lightId);
		assertThat(matchingService.findMatches("light@example.com", MatchingMode.RECOMMENDATION, 0, 20))
			.extracting(MatchResult::userId)
			.contains(deepId);
	}

	private void register(String email, String displayName, Set<String> interests) {
		register(email, displayName, interests, ProfileStateType.LIGHT_TALK);
	}

	private void register(String email, String displayName, Set<String> interests, ProfileStateType state) {
		authService.register(new RegisterUserCommand(
			email,
			"password123",
			displayName,
			24,
			"Открыта к общению",
			"Minsk",
			interests,
			state
		));
	}
}
