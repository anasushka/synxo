package com.synxo.domain.strategy;

import com.synxo.domain.enums.MatchingMode;
import com.synxo.domain.model.Profile;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RecommendationStrategy implements MatchingStrategy {

	private static final ScoreWeights WEIGHTS = new ScoreWeights(45, 15, 20, 10, 10);

	private final CompatibilityScorer scorer = new CompatibilityScorer();

	@Override
	public MatchingMode getMode() {
		return MatchingMode.RECOMMENDATION;
	}

	@Override
	public List<ScoredProfile> rank(Profile source, List<Profile> candidates, MatchingContext context) {
		return candidates.stream()
			.map(candidate -> new ScoredProfile(candidate, scorer.score(source, candidate, context, WEIGHTS)))
			.sorted(Comparator
				.comparingDouble((ScoredProfile scoredProfile) -> scoredProfile.score().total())
				.reversed()
				.thenComparing(scoredProfile -> scoredProfile.profile().getLastActiveAt(), Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(scoredProfile -> scoredProfile.profile().getId(), Comparator.nullsLast(Comparator.naturalOrder())))
			.toList();
	}
}
