package com.synxo.repository;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.model.Profile;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

	Optional<Profile> findByUserEmail(String email);

	Optional<Profile> findByUserId(Long userId);

	@Query("""
		select distinct p from Profile p
		where p.user.id <> :userId
		  and p.state <> :hiddenState
		  and (:activeAfter is null or p.lastActiveAt > :activeAfter)
		""")
	List<Profile> findVisibleCandidates(
		@Param("userId") Long userId,
		@Param("hiddenState") ProfileStateType hiddenState,
		@Param("activeAfter") LocalDateTime activeAfter
	);

	@Query("""
		select p from Profile p
		join p.interests interest
		where p.user.id <> :userId
		  and p.state <> :hiddenState
		  and (:activeAfter is null or p.lastActiveAt > :activeAfter)
		  and interest in :interests
		group by p
		having count(distinct interest) >= :minimumSharedInterests
		""")
	List<Profile> findVisibleCandidatesBySharedInterests(
		@Param("userId") Long userId,
		@Param("hiddenState") ProfileStateType hiddenState,
		@Param("activeAfter") LocalDateTime activeAfter,
		@Param("interests") Collection<String> interests,
		@Param("minimumSharedInterests") long minimumSharedInterests
	);
}
