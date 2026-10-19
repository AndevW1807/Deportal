package com.deportal.application.auth.register;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import com.deportal.application.auth.AuthenticatedUser;
import com.deportal.application.auth.failure.InvalidRegistration;
import com.deportal.application.auth.failure.PasswordTooLong;
import com.deportal.application.auth.port.PasswordHasher;
import com.deportal.domain.user.failure.EmailAlreadyUsed;
import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserAccount;
import com.deportal.domain.user.UserRepository;
import com.deportal.domain.user.UserRole;

public final class Register {

	private static final int MAX_NAME_LENGTH = 120;

	private static final int MAX_EMAIL_LENGTH = 180;

	private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	private final UserRepository users;

	private final PasswordHasher passwords;

	public Register(UserRepository users, PasswordHasher passwords) {
		this.users = users;
		this.passwords = passwords;
	}

	public AuthenticatedUser execute(String name, String email, String password, String confirmPassword) {
		String normalizedName = normalizeName(name);
		String normalizedEmail = normalizeEmail(email);
		requirePassword(password, confirmPassword);
		Optional<UserAccount> existing = users.findByEmail(normalizedEmail);
		if (existing.isPresent() && !existing.get().deleted()) {
			throw new EmailAlreadyUsed();
		}
		UserAccount saved = users.save(new UserAccount(null, normalizedName, normalizedEmail, passwords.hash(password),
				UserRole.CUSTOMER, MembershipType.NON_MEMBER, false));
		return new AuthenticatedUser(saved.id(), saved.name(), saved.email(), saved.role(), saved.type());
	}

	private static String normalizeName(String name) {
		if (name == null) {
			throw new InvalidRegistration();
		}
		String trimmed = name.trim();
		if (trimmed.isEmpty() || trimmed.length() > MAX_NAME_LENGTH) {
			throw new InvalidRegistration();
		}
		return trimmed;
	}

	private static String normalizeEmail(String email) {
		if (email == null) {
			throw new InvalidRegistration();
		}
		String normalized = email.trim().toLowerCase(Locale.ROOT);
		if (normalized.length() > MAX_EMAIL_LENGTH || !EMAIL.matcher(normalized).matches()) {
			throw new InvalidRegistration();
		}
		return normalized;
	}

	private static void requirePassword(String password, String confirmPassword) {
		if (password == null || password.isBlank() || confirmPassword == null || !password.equals(confirmPassword)) {
			throw new InvalidRegistration();
		}
		if (password.getBytes(StandardCharsets.UTF_8).length > PasswordTooLong.MAX_BYTES) {
			throw new PasswordTooLong();
		}
	}

}
