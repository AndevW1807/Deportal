package com.deportal.application.auth;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

import com.deportal.domain.user.UserAccount;
import com.deportal.domain.user.UserRepository;

public final class Login {

	static final int MAX_PASSWORD_BYTES = 72;

	private final UserRepository users;
	private final PasswordMatcher passwords;

	public Login(UserRepository users, PasswordMatcher passwords) {
		this.users = users;
		this.passwords = passwords;
	}

	public AuthenticatedUser execute(String email, String password) {
		if (password == null) {
			throw new InvalidCredentials();
		}
		if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
			throw new PasswordTooLong();
		}
		String normalized = normalize(email);
		Optional<UserAccount> found = users.findByEmail(normalized);
		String hash = found.map(user -> user.passwordHash()).orElseGet(passwords::dummyHash);
		boolean passwordMatches = passwords.matches(password, hash);
		if (found.isEmpty() || found.get().deleted() || !passwordMatches) {
			throw new InvalidCredentials();
		}
		UserAccount user = found.get();
		return new AuthenticatedUser(user.id(), user.name(), user.email(), user.role(), user.type());
	}

	private static String normalize(String email) {
		if (email == null || email.isBlank()) {
			throw new InvalidCredentials();
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}

}
