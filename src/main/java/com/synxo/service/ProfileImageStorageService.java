package com.synxo.service;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStorageService {

	String store(Long profileId, MultipartFile file, String currentPhotoUrl);
}
