package com.synxo.domain.state;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.model.Profile;
import java.util.List;

public class LightTalkState implements ProfileState {

	@Override
	public List<Profile> search(Profile current, List<Profile> candidates) {
		return visibleCandidates(current, candidates)
			.filter(Profile::isRecentlyActive)
			.filter(candidate -> current.commonInterestCount(candidate) >= 1)
			.toList();
	}

	@Override
	public boolean isDisplayedInFeed(Profile current, Profile viewer) {
		return current.isRecentlyActive();
	}

	@Override
	public ProfileStateType getType() {
		return ProfileStateType.LIGHT_TALK;
	}
}
