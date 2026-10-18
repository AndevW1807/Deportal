package com.deportal.infrastructure.web.user;

import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserAccount;

public record CustomerResponse(Long id, String name, String email, MembershipType membership) {

	static CustomerResponse from(UserAccount account) {
		return new CustomerResponse(account.id(), account.name(), account.email(), account.type());
	}

}
