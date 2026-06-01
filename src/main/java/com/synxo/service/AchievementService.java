package com.synxo.service;

import com.synxo.service.model.AchievementView;
import java.util.List;

public interface AchievementService {

	void evaluateForUser(Long userId);

	List<AchievementView> getAchievements(String email);
}
