package com.deportal.application.user;

import java.util.List;

import com.deportal.domain.user.UserAccount;
import com.deportal.domain.user.UserRepository;

public final class ListCustomers {

	private final UserRepository users;

	public ListCustomers(UserRepository users) {
		this.users = users;
	}

	public List<UserAccount> execute() {
		return users.findActiveCustomers();
	}

}
