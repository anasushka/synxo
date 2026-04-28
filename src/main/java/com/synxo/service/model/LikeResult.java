package com.synxo.service.model;

public record LikeResult(
	boolean likedByYou,
	boolean likedYou,
	boolean mutualLike,
	boolean created
) {
}
