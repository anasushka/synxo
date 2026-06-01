package com.synxo.service;

import com.synxo.domain.enums.AchievementType;
import com.synxo.domain.enums.ProfileStateType;
import com.synxo.service.command.RegisterUserCommand;
import com.synxo.service.command.SendMessageCommand;
import com.synxo.service.model.AchievementView;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AchievementIntegrationTest {

	@Autowired
	private AuthService authService;

	@Autowired
	private MatchingService matchingService;

	@Autowired
	private ChatService chatService;

	@Autowired
	private AchievementService achievementService;

	@Test
	void unlocksFirstMatchAndFirstDialogAchievements() {
		register("mira@example.com", "Mira", Set.of("Кошки", "Кофе", "Чтение", "Новые друзья", "Кино"));
		register("rita@example.com", "Rita", Set.of("Кошки", "Кофе", "Театр", "Новые друзья", "Музыка"));

		Long miraId = authService.getCurrentUser("mira@example.com").getId();
		Long ritaId = authService.getCurrentUser("rita@example.com").getId();

		matchingService.likeProfile("mira@example.com", ritaId);
		matchingService.likeProfile("rita@example.com", miraId);
		chatService.sendMessage("mira@example.com", new SendMessageCommand(ritaId, "Привет"));

		List<AchievementView> achievements = achievementService.getAchievements("mira@example.com");

		assertThat(achievements)
			.extracting(AchievementView::code)
			.contains(AchievementType.FIRST_MATCH.name(), AchievementType.FIRST_DIALOG.name());
	}

	private void register(String email, String displayName, Set<String> interests) {
		authService.register(new RegisterUserCommand(
			email,
			"password123",
			displayName,
			24,
			"Открыта к общению и любит длинные разговоры",
			"Minsk",
			interests,
			ProfileStateType.DEEP_SEARCH
		));
	}
}
