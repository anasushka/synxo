package com.synxo.web.dto.response;

import java.time.LocalDate;

public record DailyMatchResponse(
	LocalDate generatedFor,
	MatchResponse match
) {
}
