package com.deportal.infrastructure.security;

import com.deportal.application.auth.port.PasswordHasher;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements PasswordHasher {

	private final PasswordEncoder encoder = new BCryptPasswordEncoder(BcryptPasswordMatcher.COST);

	@Override
	public String hash(String rawPassword) {
		return encoder.encode(rawPassword);
	}

}
