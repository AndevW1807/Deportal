package com.deportal.application.auth.session;

import com.deportal.application.auth.AuthenticatedUser;
import com.deportal.application.auth.failure.InvalidCredentials;
import com.deportal.domain.user.UserAccount;
import com.deportal.domain.user.UserRepository;

public final class CurrentUser {

	private final UserRepository users;

	public CurrentUser(UserRepository users) {
		this.users = users;
	}

	public AuthenticatedUser execute(Long id) {
		if (id == null) {
			throw new InvalidCredentials();
		}
		UserAccount user = users.findActiveById(id).orElseThrow(InvalidCredentials::new);
		if (user.deleted()) {
			throw new InvalidCredentials();
		}
		return new AuthenticatedUser(user.id(), user.name(), user.email(), user.role(), user.type());
	}

}
