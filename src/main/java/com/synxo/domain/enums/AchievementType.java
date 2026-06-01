package com.synxo.domain.enums;

public enum AchievementType {
	FIRST_MATCH("Первый мэтч", "Получена первая взаимная симпатия."),
	FIRST_DIALOG("Первый диалог", "Начат первый диалог после взаимного лайка."),
	TEN_MUTUAL_LIKES("10 взаимных лайков", "Собрано десять взаимных симпатий."),
	PROFILE_COMPLETE("Профиль заполнен", "Добавлены фото, био и расширенный набор интересов."),
	ACTIVE_WEEK("Неделя активности", "Активность поддерживалась семь дней подряд.");

	private final String title;
	private final String description;

	AchievementType(String title, String description) {
		this.title = title;
		this.description = description;
	}

	public String title() {
		return title;
	}

	public String description() {
		return description;
	}

	public String notificationMessage() {
		return "Достижение разблокировано: %s.".formatted(title);
	}
}
