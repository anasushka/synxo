package com.synxo.domain.model;

import com.synxo.domain.enums.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Table(name = "users")
public class User {

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String email;

	@ToString.Exclude
	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String displayName;

	@Column(nullable = false)
	private Integer age;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private UserRole role = UserRole.USER;

	@Column(nullable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	private Profile profile;

	public void attachProfile(Profile profile) {
		this.profile = profile;
		if (profile != null) {
			profile.setUser(this);
		}
	}
}
