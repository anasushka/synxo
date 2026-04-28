package com.synxo.web.dto.response;

import java.util.List;

public record InterestCategoryResponse(
	String id,
	String label,
	List<String> options
) {
}
