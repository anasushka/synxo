package com.synxo.domain.strategy;

import com.synxo.domain.enums.MatchingMode;
import com.synxo.domain.model.Profile;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RecommendationStrategy implements MatchingStrategy {

	@Override
	public MatchingMode getMode() {
		return MatchingMode.RECOMMENDATION;
	}

	@Override
	public List<Profile> rank(Profile source, List<Profile> candidates) {
		return candidates.stream()
			.sorted(Comparator
				.comparingLong((Profile candidate) -> source.commonInterestCount(candidate))
				.reversed()
				.thenComparing(Profile::getLastActiveAt, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(Profile::getId, Comparator.nullsLast(Comparator.naturalOrder())))
			.toList();
	}
}
