package com.synxo.repository;

import com.synxo.domain.model.ProfileLike;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileLikeRepository extends JpaRepository<ProfileLike, Long> {

	boolean existsByLikerIdAndLikedId(Long likerId, Long likedId);

	@Query("""
		select pl.liked.id from ProfileLike pl
		where pl.liker.id = :userId
		""")
	Set<Long> findLikedUserIds(@Param("userId") Long userId);

	@Query("""
		select pl.liker.id from ProfileLike pl
		where pl.liked.id = :userId
		""")
	Set<Long> findLikedByUserIds(@Param("userId") Long userId);
}
