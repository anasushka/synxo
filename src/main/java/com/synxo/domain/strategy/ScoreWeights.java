package com.synxo.domain.strategy;

import com.synxo.domain.model.Profile;

public record ScoreWeights(
	double interest,
	double distance,
	double intention,
	double activity,
	double social
) {

	public double total() {
		return interest + distance + intention + activity + social;
	}

	public ScoreWeights personalized(Profile source) {
		if (source == null || !source.hasPersonalizedMatching()) {
			return this;
		}

		ScoreWeights personalizedWeights = new ScoreWeights(
			scale(interest, source.getInterestPriority()),
			scale(distance, source.getDistancePriority()),
			scale(intention, source.getIntentionPriority()),
			scale(activity, source.getActivityPriority()),
			scale(social, source.getSocialPriority())
		);

		return personalizedWeights.total() <= 0 ? this : personalizedWeights;
	}

	private double scale(double weight, Integer priority) {
		double normalizedPriority = priority == null ? 100.0 : Math.max(0, priority);
		return weight * (normalizedPriority / 100.0);
	}
}
