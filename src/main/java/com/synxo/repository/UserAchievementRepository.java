package com.synxo.repository;

import com.synxo.domain.enums.AchievementType;
import com.synxo.domain.model.UserAchievement;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

	boolean existsByUserIdAndType(Long userId, AchievementType type);

	List<UserAchievement> findByUserIdOrderByUnlockedAtAscIdAsc(Long userId);

	Set<UserAchievement> findByUserId(Long userId);
}
