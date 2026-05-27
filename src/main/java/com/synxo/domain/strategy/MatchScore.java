package com.synxo.domain.strategy;

public record MatchScore(
	double total,
	double interest,
	double distance,
	double intention,
	double activity,
	double social
) {
}
