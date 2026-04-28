package com.synxo.domain.model;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.state.ProfileState;
import com.synxo.domain.state.ProfileStates;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "profiles")
public class Profile {

	private static final double EARTH_RADIUS_KM = 6371.0;

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	@ToString.Exclude
	private User user;

	@Column(length = 500)
	private String bio;

	@Column(length = 255)
	private String photoUrl;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private Double latitude;

	@Column(nullable = false)
	private Double longitude;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private ProfileStateType state = ProfileStateType.DEEP_SEARCH;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "profile_interests", joinColumns = @JoinColumn(name = "profile_id"))
	@Column(name = "interest", nullable = false)
	@Builder.Default
	private Set<String> interests = new HashSet<>();

	@Column(nullable = false)
	@Builder.Default
	private LocalDateTime lastActiveAt = LocalDateTime.now();

	public List<Profile> search(List<Profile> candidates) {
		return resolveState().search(this, new ArrayList<>(candidates));
	}

	public boolean isDisplayedInFeed(Profile viewer) {
		return resolveState().isDisplayedInFeed(this, viewer);
	}

	public boolean isRecentlyActive() {
		return lastActiveAt != null && lastActiveAt.isAfter(LocalDateTime.now().minusDays(7));
	}

	public long commonInterestCount(Profile other) {
		return sharedInterests(other).size();
	}

	public Set<String> sharedInterests(Profile other) {
		if (other == null || interests == null || other.getInterests() == null) {
			return Set.of();
		}

		Set<String> shared = interests.stream()
			.filter(other.getInterests()::contains)
			.collect(Collectors.toCollection(LinkedHashSet::new));

		return Collections.unmodifiableSet(shared);
	}

	public double distanceTo(Profile other) {
		if (other == null || latitude == null || longitude == null || other.getLatitude() == null || other.getLongitude() == null) {
			return Double.MAX_VALUE;
		}

		double latDistance = Math.toRadians(other.getLatitude() - latitude);
		double lonDistance = Math.toRadians(other.getLongitude() - longitude);
		double startLat = Math.toRadians(latitude);
		double endLat = Math.toRadians(other.getLatitude());

		double a = Math.pow(Math.sin(latDistance / 2), 2)
			+ Math.cos(startLat) * Math.cos(endLat) * Math.pow(Math.sin(lonDistance / 2), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return EARTH_RADIUS_KM * c;
	}

	public void markActive() {
		lastActiveAt = LocalDateTime.now();
	}

	private ProfileState resolveState() {
		return ProfileStates.from(state);
	}
}
