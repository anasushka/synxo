package com.synxo.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
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
@Table(
	name = "profile_likes",
	uniqueConstraints = @UniqueConstraint(name = "uk_profile_like_pair", columnNames = {"liker_id", "liked_id"})
)
public class ProfileLike {

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "liker_id", nullable = false)
	@ToString.Exclude
	private User liker;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "liked_id", nullable = false)
	@ToString.Exclude
	private User liked;

	@Column(nullable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();
}
