package com.synxo.domain.state;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.model.Profile;
import java.util.List;

public class GhostModeState implements ProfileState {

	@Override
	public List<Profile> search(Profile current, List<Profile> candidates) {
		return visibleCandidates(current, candidates).toList();
	}

	@Override
	public boolean isDisplayedInFeed(Profile current, Profile viewer) {
		return false;
	}

	@Override
	public ProfileStateType getType() {
		return ProfileStateType.GHOST_MODE;
	}
}
