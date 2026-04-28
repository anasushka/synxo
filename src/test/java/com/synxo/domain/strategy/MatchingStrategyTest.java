package com.synxo.domain.strategy;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.model.Profile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingStrategyTest {

	private final RecommendationStrategy recommendationStrategy = new RecommendationStrategy();
	private final ProximityStrategy proximityStrategy = new ProximityStrategy();

	@Test
	void recommendationPrefersMoreSharedInterests() {
		Profile source = profile(1L, 53.9, 27.56, Set.of("music", "travel", "books"));
		Profile strongCandidate = profile(2L, 53.91, 27.57, Set.of("music", "travel"));
		Profile mediumCandidate = profile(3L, 53.92, 27.58, Set.of("music"));

		List<Profile> ranked = recommendationStrategy.rank(source, List.of(mediumCandidate, strongCandidate));

		assertThat(ranked).containsExactly(strongCandidate, mediumCandidate);
	}

	@Test
	void proximityPrefersNearestProfiles() {
		Profile source = profile(1L, 53.9, 27.56, Set.of("music"));
		Profile nearest = profile(2L, 53.9005, 27.5605, Set.of("music"));
		Profile farthest = profile(3L, 55.75, 37.61, Set.of("music"));

		List<Profile> ranked = proximityStrategy.rank(source, List.of(farthest, nearest));

		assertThat(ranked).containsExactly(nearest, farthest);
	}

	private Profile profile(Long id, double latitude, double longitude, Set<String> interests) {
		return Profile.builder()
			.id(id)
			.city("Minsk")
			.bio("Candidate %s".formatted(id))
			.latitude(latitude)
			.longitude(longitude)
			.state(ProfileStateType.DEEP_SEARCH)
			.interests(interests)
			.lastActiveAt(LocalDateTime.now())
			.build();
	}
}
