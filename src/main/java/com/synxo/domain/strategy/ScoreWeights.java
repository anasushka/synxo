package com.synxo.domain.strategy;

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
}
