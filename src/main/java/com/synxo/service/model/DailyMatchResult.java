package com.synxo.service.model;

import java.time.LocalDate;

public record DailyMatchResult(
	LocalDate generatedFor,
	MatchResult match
) {
}
