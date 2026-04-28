package com.synxo.domain.state;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.model.Profile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileStateTest {

	@Test
	void deepSearchUsesStrictFilteringAndSkipsGhosts() {
		Profile source = profile(1L, ProfileStateType.DEEP_SEARCH, Set.of("music", "travel", "books"), 0, 53.9, 27.56);
		Profile strongCandidate = profile(2L, ProfileStateType.DEEP_SEARCH, Set.of("music", "travel"), 0, 53.91, 27.57);
		Profile weakCandidate = profile(3L, ProfileStateType.DEEP_SEARCH, Set.of("music"), 0, 53.92, 27.58);
		Profile hiddenGhost = profile(4L, ProfileStateType.GHOST_MODE, Set.of("music", "travel"), 0, 53.93, 27.59);

		List<Profile> result = source.search(List.of(strongCandidate, weakCandidate, hiddenGhost));

		assertThat(result).containsExactly(strongCandidate);
		assertThat(hiddenGhost.isDisplayedInFeed(source)).isFalse();
	}

	@Test
	void lightTalkShowsOnlyActiveProfilesWithSharedHobby() {
		Profile source = profile(1L, ProfileStateType.LIGHT_TALK, Set.of("music", "coffee"), 0, 53.9, 27.56);
		Profile activeCandidate = profile(2L, ProfileStateType.DEEP_SEARCH, Set.of("music"), 0, 53.9, 27.57);
		Profile inactiveCandidate = profile(3L, ProfileStateType.DEEP_SEARCH, Set.of("music", "coffee"), 10, 53.9, 27.58);

		List<Profile> result = source.search(List.of(activeCandidate, inactiveCandidate));

		assertThat(result).containsExactly(activeCandidate);
		assertThat(source.isDisplayedInFeed(activeCandidate)).isTrue();
	}

	@Test
	void ghostModeCanBrowseOthersButNeverAppearsInFeed() {
		Profile source = profile(1L, ProfileStateType.GHOST_MODE, Set.of("art"), 0, 53.9, 27.56);
		Profile visibleCandidate = profile(2L, ProfileStateType.DEEP_SEARCH, Set.of("art"), 0, 53.9, 27.57);
		Profile hiddenCandidate = profile(3L, ProfileStateType.GHOST_MODE, Set.of("art"), 0, 53.9, 27.58);

		List<Profile> result = source.search(List.of(visibleCandidate, hiddenCandidate));

		assertThat(result).containsExactly(visibleCandidate);
		assertThat(source.isDisplayedInFeed(visibleCandidate)).isFalse();
	}

	private Profile profile(
		Long id,
		ProfileStateType state,
		Set<String> interests,
		int inactiveDays,
		double latitude,
		double longitude
	) {
		return Profile.builder()
			.id(id)
			.city("Minsk")
			.bio("Profile %s".formatted(id))
			.latitude(latitude)
			.longitude(longitude)
			.state(state)
			.interests(interests)
			.lastActiveAt(LocalDateTime.now().minusDays(inactiveDays))
			.build();
	}
}
