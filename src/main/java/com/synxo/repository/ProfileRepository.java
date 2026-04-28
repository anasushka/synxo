package com.synxo.repository;

import com.synxo.domain.model.Profile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

	Optional<Profile> findByUserEmail(String email);

	Optional<Profile> findByUserId(Long userId);

	List<Profile> findByUserIdNot(Long userId);
}
