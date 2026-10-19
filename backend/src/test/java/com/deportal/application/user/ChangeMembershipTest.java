package com.deportal.application.user;

import java.util.Optional;

import com.deportal.domain.user.failure.CustomerNotFound;
import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserAccount;
import com.deportal.domain.user.UserRepository;
import com.deportal.domain.user.UserRole;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangeMembershipTest {

	@Test
	void changesACustomerMembershipAndKeepsTheRole() {
		MemoryUsers users = new MemoryUsers(customer(2L, MembershipType.NON_MEMBER));

		UserAccount saved = new ChangeMembership(users).execute(2L, MembershipType.MEMBER);

		assertThat(saved.type()).isEqualTo(MembershipType.MEMBER);
		assertThat(saved.role()).isEqualTo(UserRole.CUSTOMER);
		assertThat(users.saved).isSameAs(saved);
	}

	@Test
	void rejectsAnAdmin() {
		MemoryUsers users = new MemoryUsers(
				new UserAccount(1L, "Admin", "admin@admin.com", "hash", UserRole.ADMIN, MembershipType.NON_MEMBER, false));

		assertThatThrownBy(() -> new ChangeMembership(users).execute(1L, MembershipType.MEMBER))
			.isInstanceOf(CustomerNotFound.class);
		assertThat(users.saved).isNull();
	}

	@Test
	void rejectsAnUnknownUser() {
		MemoryUsers users = new MemoryUsers();

		assertThatThrownBy(() -> new ChangeMembership(users).execute(9L, MembershipType.MEMBER))
			.isInstanceOf(CustomerNotFound.class);
	}

	private static UserAccount customer(Long id, MembershipType type) {
		return new UserAccount(id, "Ana", "ana@deportal.test", "hash", UserRole.CUSTOMER, type, false);
	}

	private static final class MemoryUsers implements UserRepository {

		private final UserAccount account;
		private UserAccount saved;

		private MemoryUsers() {
			this.account = null;
		}

		private MemoryUsers(UserAccount account) {
			this.account = account;
		}

		@Override
		public Optional<UserAccount> findByEmail(String email) {
			return Optional.empty();
		}

		@Override
		public Optional<UserAccount> findActiveById(Long id) {
			if (account != null && account.id().equals(id) && !account.deleted()) {
				return Optional.of(account);
			}
			return Optional.empty();
		}

		@Override
		public java.util.List<UserAccount> findActiveCustomers() {
			return java.util.List.of();
		}

		@Override
		public UserAccount save(UserAccount account) {
			saved = account;
			return account;
		}

	}

}
