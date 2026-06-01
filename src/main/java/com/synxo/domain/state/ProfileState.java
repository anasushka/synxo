package com.synxo.domain.state;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.model.Profile;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public interface ProfileState {

	List<Profile> search(Profile current, List<Profile> candidates);

	boolean isDisplayedInFeed(Profile current, Profile viewer);

	ProfileStateType getType();

	default Stream<Profile> visibleCandidates(Profile current, List<Profile> candidates) {
		return candidates.stream()
			.filter(Objects::nonNull)
			.filter(candidate -> !isSameProfile(current, candidate))
			.filter(candidate -> candidate.isDisplayedInFeed(current));
	}

	private boolean isSameProfile(Profile current, Profile candidate) {
		if (current == candidate) {
			return true;
		}

		return current.getId() != null && current.getId().equals(candidate.getId());
	}
}
