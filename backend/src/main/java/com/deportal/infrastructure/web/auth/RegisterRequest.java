package com.deportal.infrastructure.web.auth;

import java.util.Locale;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotBlank @Size(max = 120) String name, @NotBlank @Email @Size(max = 180) String email,
		@NotBlank String password, @NotBlank String confirmPassword) {

	public RegisterRequest {
		name = name == null ? null : name.trim();
		email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}

}
