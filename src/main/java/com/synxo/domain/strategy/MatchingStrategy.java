package com.synxo.domain.strategy;

import com.synxo.domain.enums.MatchingMode;
import com.synxo.domain.model.Profile;
import java.util.List;

public interface MatchingStrategy {

	MatchingMode getMode();

	List<Profile> rank(Profile source, List<Profile> candidates);
}
