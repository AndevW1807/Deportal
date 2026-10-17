package com.deportal.infrastructure.security;

import com.deportal.application.auth.PasswordMatcher;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordMatcher implements PasswordMatcher {

	static final int COST = 12;

	private static final String DUMMY_HASH = "$2b$12$8CODaIOcwM.gzxUSuuAtQOPPBmViCQhyfACHW6t8t961cgX82XVSu";

	private final PasswordEncoder encoder = new BCryptPasswordEncoder(COST);

	@Override
	public boolean matches(String rawPassword, String passwordHash) {
		return encoder.matches(rawPassword, passwordHash);
	}

	@Override
	public String dummyHash() {
		return DUMMY_HASH;
	}

}
