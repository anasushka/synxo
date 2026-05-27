package com.synxo.domain.strategy;

import com.synxo.domain.model.Profile;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class CompatibilityScorer {

	private static final double DISTANCE_DECAY_KM = 30.0;
	private static final double ACTIVITY_WINDOW_DAYS = 14.0;
	private static final Set<String> INTENTION_INTERESTS = Set.of(
		"Серьезные отношения",
		"Легкое общение",
		"Новые друзья",
		"Спонтанные встречи",
		"Долгие переписки"
	);

	public MatchScore score(Profile source, Profile candidate, MatchingContext context, ScoreWeights weights) {
		double interest = interestScore(source, candidate);
		double distance = distanceScore(source, candidate);
		double intention = intentionScore(source, candidate);
		double activity = activityScore(candidate);
		double social = socialScore(candidate, context);

		double total = weightedAverage(weights, interest, distance, intention, activity, social);
		return new MatchScore(
			round(total),
			round(interest),
			round(distance),
			round(intention),
			round(activity),
			round(social)
		);
	}

	private double interestScore(Profile source, Profile candidate) {
		Set<String> sourceInterests = source.getInterests() == null ? Set.of() : source.getInterests();
		Set<String> candidateInterests = candidate.getInterests() == null ? Set.of() : candidate.getInterests();
		if (sourceInterests.isEmpty() && candidateInterests.isEmpty()) {
			return 0;
		}

		Set<String> union = new HashSet<>(sourceInterests);
		union.addAll(candidateInterests);
		if (union.isEmpty()) {
			return 0;
		}

		return (source.sharedInterests(candidate).size() * 100.0) / union.size();
	}

	private double distanceScore(Profile source, Profile candidate) {
		double distance = source.distanceTo(candidate);
		if (distance == Double.MAX_VALUE) {
			return 0;
		}
		return Math.exp(-distance / DISTANCE_DECAY_KM) * 100.0;
	}

	private double intentionScore(Profile source, Profile candidate) {
		Set<String> sourceIntentions = selectedIntentions(source);
		Set<String> candidateIntentions = selectedIntentions(candidate);
		if (sourceIntentions.isEmpty() || candidateIntentions.isEmpty()) {
			return 50;
		}

		Set<String> sharedIntentions = new HashSet<>(sourceIntentions);
		sharedIntentions.retainAll(candidateIntentions);
		return sharedIntentions.isEmpty() ? 20 : 100;
	}

	private Set<String> selectedIntentions(Profile profile) {
		Set<String> result = new HashSet<>();
		if (profile.getInterests() == null) {
			return result;
		}

		for (String interest : profile.getInterests()) {
			if (INTENTION_INTERESTS.contains(interest)) {
				result.add(interest);
			}
		}
		return result;
	}

	private double activityScore(Profile candidate) {
		LocalDateTime lastActiveAt = candidate.getLastActiveAt();
		if (lastActiveAt == null) {
			return 0;
		}

		double daysInactive = Math.max(0, Duration.between(lastActiveAt, LocalDateTime.now()).toHours() / 24.0);
		return Math.max(0, 100.0 * (1.0 - daysInactive / ACTIVITY_WINDOW_DAYS));
	}

	private double socialScore(Profile candidate, MatchingContext context) {
		if (context.mutualLike(candidate) || context.likedYou(candidate)) {
			return 100;
		}
		if (context.likedByYou(candidate)) {
			return 35;
		}
		return 0;
	}

	private double weightedAverage(
		ScoreWeights weights,
		double interest,
		double distance,
		double intention,
		double activity,
		double social
	) {
		double totalWeight = weights.total();
		if (totalWeight <= 0) {
			return 0;
		}

		return (
			interest * weights.interest()
				+ distance * weights.distance()
				+ intention * weights.intention()
				+ activity * weights.activity()
				+ social * weights.social()
		) / totalWeight;
	}

	private double round(double value) {
		return Math.round(value * 10.0) / 10.0;
	}
}
