package com.synxo.service.impl;

import com.synxo.domain.exception.StorageException;
import com.synxo.service.ProfileImageStorageService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemProfileImageStorageService implements ProfileImageStorageService {

	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
	private static final Set<String> ALLOWED_TYPES = Set.of(
		"image/jpeg",
		"image/png",
		"image/webp",
		"image/gif"
	);

	private final Path uploadDirectory;

	public FileSystemProfileImageStorageService(@Value("${app.storage.upload-dir:./uploads}") String uploadDirectory) {
		this.uploadDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
	}

	@PostConstruct
	public void createDirectoryIfMissing() {
		try {
			Files.createDirectories(uploadDirectory);
		} catch (IOException exception) {
			throw new StorageException("Unable to initialize upload directory", exception);
		}
	}

	@Override
	public String store(Long profileId, MultipartFile file, String currentPhotoUrl) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Please choose an image before uploading");
		}

		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
			throw new IllegalArgumentException("Only JPG, PNG, WEBP or GIF images are allowed");
		}

		String extension = resolveExtension(file.getOriginalFilename());
		String fileName = "profile-%s-%s.%s".formatted(profileId, UUID.randomUUID(), extension);
		Path targetPath = uploadDirectory.resolve(fileName).normalize();

		try {
			Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
			deleteIfPresent(currentPhotoUrl);
			return "/uploads/" + fileName;
		} catch (IOException exception) {
			throw new StorageException("Unable to save profile image", exception);
		}
	}

	private void deleteIfPresent(String currentPhotoUrl) throws IOException {
		if (currentPhotoUrl == null || !currentPhotoUrl.startsWith("/uploads/")) {
			return;
		}

		Path currentPath = uploadDirectory.resolve(currentPhotoUrl.replace("/uploads/", "")).normalize();
		if (Files.exists(currentPath)) {
			Files.delete(currentPath);
		}
	}

	private String resolveExtension(String originalFilename) {
		if (originalFilename == null || !originalFilename.contains(".")) {
			return "jpg";
		}

		String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new IllegalArgumentException("Unsupported image file extension");
		}

		return extension;
	}
}
