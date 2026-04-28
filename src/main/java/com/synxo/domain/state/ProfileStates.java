package com.synxo.domain.state;

import com.synxo.domain.enums.ProfileStateType;
import java.util.EnumMap;
import java.util.Map;

public final class ProfileStates {

	private static final Map<ProfileStateType, ProfileState> STATES = new EnumMap<>(ProfileStateType.class);

	static {
		register(new DeepSearchState());
		register(new LightTalkState());
		register(new GhostModeState());
	}

	private ProfileStates() {
	}

	public static ProfileState from(ProfileStateType type) {
		return STATES.getOrDefault(type, STATES.get(ProfileStateType.DEEP_SEARCH));
	}

	private static void register(ProfileState profileState) {
		STATES.put(profileState.getType(), profileState);
	}
}
