package com.synxo.domain.state;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.model.Profile;
import java.util.List;

public class DeepSearchState implements ProfileState {

	@Override
	public List<Profile> search(Profile current, List<Profile> candidates) {
		return visibleCandidates(current, candidates)
			.filter(candidate -> current.commonInterestCount(candidate) >= 2)
			.toList();
	}

	@Override
	public boolean isDisplayedInFeed(Profile current, Profile viewer) {
		return true;
	}

	@Override
	public ProfileStateType getType() {
		return ProfileStateType.DEEP_SEARCH;
	}
}
