package com.synxo.service;

import com.synxo.service.model.LikeResult;
import com.synxo.service.model.LikeSnapshot;
import java.util.Set;

public interface ProfileLikeService {

	LikeSnapshot getSnapshot(Long userId);

	LikeResult like(Long likerUserId, Long likedUserId);

	boolean isMutualLike(Long firstUserId, Long secondUserId);

	Set<Long> findMutualUserIds(Long userId);
}
