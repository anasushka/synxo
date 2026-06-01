package com.synxo.service;

import com.synxo.domain.enums.MatchingMode;
import com.synxo.service.model.DailyMatchResult;
import com.synxo.service.model.MatchResult;
import java.util.List;

public interface MatchingService {

	List<MatchResult> findMatches(String email, MatchingMode mode, int page, int size);

	DailyMatchResult findDailyMatch(String email, MatchingMode mode);

	MatchResult likeProfile(String email, Long targetUserId);
}
