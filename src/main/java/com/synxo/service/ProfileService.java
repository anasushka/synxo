package com.synxo.service;

import com.synxo.domain.enums.ProfileStateType;
import com.synxo.domain.model.Profile;
import com.synxo.service.command.UpdateProfileCommand;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

	Profile getCurrentProfile(String email);

	Profile changeState(String email, ProfileStateType state);

	Profile updateProfile(String email, UpdateProfileCommand command);

	Profile updatePhoto(String email, MultipartFile file);
}
