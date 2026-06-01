package com.synxo.domain.strategy;

import com.synxo.domain.enums.MatchingMode;
import com.synxo.domain.model.Profile;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProximityStrategy implements MatchingStrategy {

	private static final ScoreWeights WEIGHTS = new ScoreWeights(25, 45, 15, 5, 10);

	private final CompatibilityScorer scorer = new CompatibilityScorer();

	@Override
	public MatchingMode getMode() {
		return MatchingMode.PROXIMITY;
	}

	@Override
	public List<ScoredProfile> rank(Profile source, List<Profile> candidates, MatchingContext context) {
		ScoreWeights weights = WEIGHTS.personalized(source);
		return candidates.stream()
			.map(candidate -> new ScoredProfile(candidate, scorer.score(source, candidate, context, weights)))
			.sorted(Comparator
				.comparingDouble((ScoredProfile scoredProfile) -> scoredProfile.score().total())
				.reversed()
				.thenComparing(scoredProfile -> source.distanceTo(scoredProfile.profile()))
				.thenComparing(scoredProfile -> scoredProfile.profile().getId(), Comparator.nullsLast(Comparator.naturalOrder())))
			.toList();
	}
}
