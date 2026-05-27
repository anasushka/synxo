package com.synxo.service.impl;

import com.synxo.service.LocationService;
import com.synxo.service.model.Coordinates;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NominatimLocationService implements LocationService {

	private static final Duration MIN_REQUEST_INTERVAL = Duration.ofSeconds(1);
	private static final Pattern LATITUDE_PATTERN = Pattern.compile("\"lat\"\\s*:\\s*\"([-+]?\\d+(?:\\.\\d+)?)\"");
	private static final Pattern LONGITUDE_PATTERN = Pattern.compile("\"lon\"\\s*:\\s*\"([-+]?\\d+(?:\\.\\d+)?)\"");

	private final HttpClient httpClient = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(5))
		.build();
	private final Map<String, Coordinates> cityCache = new ConcurrentHashMap<>();
	private final Object rateLimitLock = new Object();

	private final boolean geocodingEnabled;
	private final String baseUrl;
	private final String userAgent;
	private final Coordinates fallbackCoordinates;
	private Instant lastRequestAt = Instant.EPOCH;

	public NominatimLocationService(
		@Value("${app.location.geocoding.enabled:true}") boolean geocodingEnabled,
		@Value("${app.location.nominatim.base-url:https://nominatim.openstreetmap.org/search}") String baseUrl,
		@Value("${app.location.nominatim.user-agent:SynxoCoursework/1.0}") String userAgent,
		@Value("${app.location.default-latitude:53.9006}") double defaultLatitude,
		@Value("${app.location.default-longitude:27.5590}") double defaultLongitude
	) {
		this.geocodingEnabled = geocodingEnabled;
		this.baseUrl = baseUrl;
		this.userAgent = userAgent;
		this.fallbackCoordinates = new Coordinates(defaultLatitude, defaultLongitude);
	}

	@Override
	public Coordinates resolveCity(String city) {
		String normalizedCity = normalizeCity(city);
		String cacheKey = normalizedCity.toLowerCase(Locale.ROOT);
		return cityCache.computeIfAbsent(cacheKey, ignored -> geocodingEnabled
			? fetchCoordinates(normalizedCity)
			: fallbackCoordinates);
	}

	@Override
	public Coordinates preciseCoordinates(Double latitude, Double longitude) {
		if (latitude == null || longitude == null) {
			throw new IllegalArgumentException("Precise latitude and longitude are required.");
		}
		return new Coordinates(latitude, longitude);
	}

	private Coordinates fetchCoordinates(String city) {
		waitForRateLimit();

		String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
		URI uri = URI.create("%s?format=jsonv2&limit=1&addressdetails=0&accept-language=ru&q=%s".formatted(baseUrl, encodedCity));
		HttpRequest request = HttpRequest.newBuilder(uri)
			.timeout(Duration.ofSeconds(10))
			.header("User-Agent", userAgent)
			.header("Accept", "application/json")
			.GET()
			.build();

		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new IllegalArgumentException("City coordinates service returned HTTP %s.".formatted(response.statusCode()));
			}
			return parseCoordinates(city, response.body());
		} catch (IOException exception) {
			throw new IllegalArgumentException("Unable to resolve city coordinates right now.", exception);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("City coordinates lookup was interrupted.", exception);
		}
	}

	private Coordinates parseCoordinates(String city, String responseBody) {
		if (responseBody == null || responseBody.isBlank() || "[]".equals(responseBody.trim())) {
			throw new IllegalArgumentException("Could not find coordinates for city '%s'.".formatted(city));
		}

		Matcher latitude = LATITUDE_PATTERN.matcher(responseBody);
		Matcher longitude = LONGITUDE_PATTERN.matcher(responseBody);
		if (!latitude.find() || !longitude.find()) {
			throw new IllegalArgumentException("Coordinates response for city '%s' is incomplete.".formatted(city));
		}

		return new Coordinates(Double.parseDouble(latitude.group(1)), Double.parseDouble(longitude.group(1)));
	}

	private void waitForRateLimit() {
		synchronized (rateLimitLock) {
			long waitMs = MIN_REQUEST_INTERVAL.toMillis() - Duration.between(lastRequestAt, Instant.now()).toMillis();
			if (waitMs > 0) {
				try {
					Thread.sleep(waitMs);
				} catch (InterruptedException exception) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException("City coordinates lookup was interrupted.", exception);
				}
			}
			lastRequestAt = Instant.now();
		}
	}

	private String normalizeCity(String city) {
		if (city == null || city.isBlank()) {
			throw new IllegalArgumentException("City is required.");
		}
		return city.trim();
	}
}
