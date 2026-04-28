package com.synxo.service.util;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class ServiceUtils {

	private ServiceUtils() {
	}

	public static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	public static Set<String> normalizeInterests(Set<String> interests) {
		if (interests == null) {
			return Set.of();
		}
		return interests.stream()
			.map(String::trim)
			.filter(value -> !value.isBlank())
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
