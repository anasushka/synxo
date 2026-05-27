package com.synxo.domain.strategy;

import com.synxo.domain.model.Profile;

public record ScoredProfile(
	Profile profile,
	MatchScore score
) {
}
