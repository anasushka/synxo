package com.synxo.service;

import com.synxo.domain.model.User;
import com.synxo.service.command.RegisterUserCommand;

public interface AuthService {

	User register(RegisterUserCommand command);

	User getCurrentUser(String email);
}
