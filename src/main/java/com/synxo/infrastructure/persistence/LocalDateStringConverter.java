package com.synxo.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Converter(autoApply = false)
public class LocalDateStringConverter implements AttributeConverter<LocalDate, String> {

	@Override
	public String convertToDatabaseColumn(LocalDate attribute) {
		return attribute == null ? null : attribute.toString();
	}

	@Override
	public LocalDate convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isBlank()) {
			return null;
		}

		String normalized = dbData.trim();
		if (normalized.chars().allMatch(Character::isDigit)) {
			long epochMillis = Long.parseLong(normalized);
			return Instant.ofEpochMilli(epochMillis)
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
		}

		return LocalDate.parse(normalized);
	}
}
