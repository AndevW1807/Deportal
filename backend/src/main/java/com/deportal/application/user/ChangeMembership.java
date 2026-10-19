package com.deportal.application.user;

import com.deportal.domain.user.failure.CustomerNotFound;
import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserAccount;
import com.deportal.domain.user.UserRepository;
import com.deportal.domain.user.UserRole;

public final class ChangeMembership {

	private final UserRepository users;

	public ChangeMembership(UserRepository users) {
		this.users = users;
	}

	public UserAccount execute(Long id, MembershipType membership) {
		UserAccount account = users.findActiveById(id)
			.filter(user -> user.role() == UserRole.CUSTOMER)
			.orElseThrow(CustomerNotFound::new);
		return users.save(account.withMembership(membership));
	}

}
