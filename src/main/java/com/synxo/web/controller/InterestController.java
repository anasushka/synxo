package com.synxo.web.controller;

import com.synxo.web.dto.response.InterestCategoryResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interests")
public class InterestController {

	@GetMapping
	public List<InterestCategoryResponse> list() {
		return List.of(
			new InterestCategoryResponse("animals", "Любимые животные", List.of("Кошки", "Собаки", "Лошади", "Лисы", "Панды")),
			new InterestCategoryResponse("food", "Любимая еда", List.of("Суши", "Паста", "Пицца", "Десерты", "Кофе")),
			new InterestCategoryResponse("activities", "Любимые занятия", List.of("Прогулки", "Путешествия", "Спорт", "Чтение", "Кино")),
			new InterestCategoryResponse("music", "Музыка", List.of("Поп", "Рок", "Инди", "Джаз", "Электроника")),
			new InterestCategoryResponse("vibe", "Формат общения", List.of("Серьезные отношения", "Легкое общение", "Новые друзья", "Спонтанные встречи", "Долгие переписки"))
		);
	}
}
