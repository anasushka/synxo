package com.synxo.service;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.service.command.RegisterUserCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthServiceIntegrationTest {

	@Autowired
	private AuthService authService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void registerPersistsUserRowThatCanBeReadBack() {
		authService.register(new RegisterUserCommand(
			"alice@example.com",
			"password123",
			"Alice",
			24,
			"Открыта к общению",
			"Minsk",
			53.9006,
			27.5590,
			java.util.Set.of("Кошки", "Кофе", "Чтение"),
			ProfileStateType.LIGHT_TALK
		));

		Integer userCount = jdbcTemplate.queryForObject(
			"select count(*) from users where email = ?",
			Integer.class,
			"alice@example.com"
		);
		Number userId = jdbcTemplate.queryForObject(
			"select id from users where email = ?",
			Number.class,
			"alice@example.com"
		);

		assertThat(userCount).isEqualTo(1);
		assertThat(userId).isNotNull();
		assertThat(authService.getCurrentUser("alice@example.com").getEmail()).isEqualTo("alice@example.com");
	}
}
