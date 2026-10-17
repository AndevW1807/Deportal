package com.deportal.infrastructure.web;

import com.deportal.application.auth.AuthenticatedUser;
import com.deportal.application.auth.CurrentUser;
import com.deportal.application.auth.InvalidCredentials;
import com.deportal.application.auth.Login;
import com.deportal.infrastructure.security.IssuedToken;
import com.deportal.infrastructure.security.TokenIssuer;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final Login login;
	private final CurrentUser currentUser;
	private final TokenIssuer tokens;

	public AuthController(Login login, CurrentUser currentUser, TokenIssuer tokens) {
		this.login = login;
		this.currentUser = currentUser;
		this.tokens = tokens;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		AuthenticatedUser user = login.execute(request.email(), request.password());
		IssuedToken token = tokens.issue(user);
		return new LoginResponse("Bearer", token.value(), token.expiresInSeconds());
	}

	@GetMapping("/session")
	public CurrentUserResponse session(@AuthenticationPrincipal Jwt jwt) {
		if (jwt == null || jwt.getSubject() == null) {
			throw new InvalidCredentials();
		}
		long id;
		try {
			id = Long.parseLong(jwt.getSubject());
		}
		catch (NumberFormatException exception) {
			throw new InvalidCredentials();
		}
		AuthenticatedUser user = currentUser.execute(id);
		return new CurrentUserResponse(user.id(), user.name(), user.email(), user.role().name(), user.type().name());
	}

}
