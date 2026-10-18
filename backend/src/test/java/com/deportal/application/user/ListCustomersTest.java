package com.deportal.application.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserAccount;
import com.deportal.domain.user.UserRepository;
import com.deportal.domain.user.UserRole;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListCustomersTest {

	@Test
	void listsOnlyTheActiveCustomersFromThePort() {
		UserAccount ana = customer(2L, "Ana", MembershipType.MEMBER);
		MemoryUsers users = new MemoryUsers(admin(), ana, customer(3L, "Luis", MembershipType.NON_MEMBER));

		assertThat(new ListCustomers(users).execute()).containsExactly(ana, users.accounts.get(2));
	}

	private static UserAccount admin() {
		return new UserAccount(1L, "Admin", "admin@admin.com", "hash", UserRole.ADMIN, MembershipType.NON_MEMBER,
				false);
	}

	private static UserAccount customer(Long id, String name, MembershipType type) {
		return new UserAccount(id, name, name.toLowerCase() + "@deportal.test", "hash", UserRole.CUSTOMER, type, false);
	}

	private static final class MemoryUsers implements UserRepository {

		private final List<UserAccount> accounts;

		private MemoryUsers(UserAccount... accounts) {
			this.accounts = new ArrayList<>(List.of(accounts));
		}

		@Override
		public Optional<UserAccount> findByEmail(String email) {
			return Optional.empty();
		}

		@Override
		public Optional<UserAccount> findActiveById(Long id) {
			return Optional.empty();
		}

		@Override
		public List<UserAccount> findActiveCustomers() {
			return accounts.stream().filter(account -> account.role() == UserRole.CUSTOMER && !account.deleted()).toList();
		}

		@Override
		public UserAccount save(UserAccount account) {
			return account;
		}

	}

}
