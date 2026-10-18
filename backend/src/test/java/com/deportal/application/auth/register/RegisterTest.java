package com.deportal.application.auth.register;

import java.util.Optional;

import com.deportal.application.auth.AuthenticatedUser;
import com.deportal.application.auth.failure.InvalidRegistration;
import com.deportal.application.auth.failure.PasswordTooLong;
import com.deportal.application.auth.port.PasswordHasher;
import com.deportal.domain.user.EmailAlreadyUsed;
import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserAccount;
import com.deportal.domain.user.UserRepository;
import com.deportal.domain.user.UserRole;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterTest {

	private static final String EMAIL = "ana@deportal.test";

	@Test
	void createsACustomerWhoIsNotAMember() {
		Accounts accounts = new Accounts(null);
		RecordingPasswordHasher passwords = new RecordingPasswordHasher();

		AuthenticatedUser created = new Register(accounts, passwords).execute("  Ana López  ", "  Ana@Deportal.Test ",
				"secret-value", "secret-value");

		assertThat(created.id()).isEqualTo(8L);
		assertThat(created.name()).isEqualTo("Ana López");
		assertThat(created.email()).isEqualTo(EMAIL);
		assertThat(created.role()).isEqualTo(UserRole.CUSTOMER);
		assertThat(created.type()).isEqualTo(MembershipType.NON_MEMBER);
		assertThat(accounts.saved.passwordHash()).isEqualTo("hashed-secret");
		assertThat(accounts.saved.deleted()).isFalse();
		assertThat(passwords.rawPassword).isEqualTo("secret-value");
	}

	@Test
	void rejectsAPasswordThatDoesNotMatchBeforeHashing() {
		Accounts accounts = new Accounts(null);
		RecordingPasswordHasher passwords = new RecordingPasswordHasher();

		assertThatThrownBy(() -> new Register(accounts, passwords).execute("Ana", EMAIL, "secret-value", "other"))
			.isInstanceOf(InvalidRegistration.class);

		assertThat(passwords.invocations).isZero();
		assertThat(accounts.saved).isNull();
	}

	@Test
	void rejectsPasswordsLongerThanTheBcryptLimitBeforeHashing() {
		Accounts accounts = new Accounts(null);
		RecordingPasswordHasher passwords = new RecordingPasswordHasher();

		assertThatThrownBy(() -> new Register(accounts, passwords).execute("Ana", EMAIL, "a".repeat(73), "a".repeat(73)))
			.isInstanceOf(PasswordTooLong.class);

		assertThat(passwords.invocations).isZero();
		assertThat(accounts.saved).isNull();
	}

	@Test
	void rejectsAnEmailThatIsAlreadyActive() {
		Accounts accounts = new Accounts(
				new UserAccount(1L, "Ana", EMAIL, "stored-hash", UserRole.CUSTOMER, MembershipType.NON_MEMBER, false));
		RecordingPasswordHasher passwords = new RecordingPasswordHasher();

		assertThatThrownBy(() -> new Register(accounts, passwords).execute("Ana", EMAIL, "secret-value", "secret-value"))
			.isInstanceOf(EmailAlreadyUsed.class);

		assertThat(passwords.invocations).isZero();
		assertThat(accounts.saved).isNull();
	}

	@Test
	void allowsTheEmailOfADeletedAccount() {
		Accounts accounts = new Accounts(
				new UserAccount(1L, "Ana", EMAIL, "stored-hash", UserRole.CUSTOMER, MembershipType.MEMBER, true));

		AuthenticatedUser created = new Register(accounts, new RecordingPasswordHasher()).execute("Ana", EMAIL,
				"secret-value", "secret-value");

		assertThat(created.email()).isEqualTo(EMAIL);
		assertThat(created.type()).isEqualTo(MembershipType.NON_MEMBER);
		assertThat(accounts.saved).isNotNull();
	}

	private static final class Accounts implements UserRepository {

		private final UserAccount existing;

		private UserAccount saved;

		private Accounts(UserAccount existing) {
			this.existing = existing;
		}

		@Override
		public Optional<UserAccount> findByEmail(String email) {
			return Optional.ofNullable(existing);
		}

		@Override
		public Optional<UserAccount> findActiveById(Long id) {
			return Optional.empty();
		}

		@Override
		public UserAccount save(UserAccount account) {
			saved = account;
			return new UserAccount(8L, account.name(), account.email(), account.passwordHash(), account.role(),
					account.type(), account.deleted());
		}

	}

	private static final class RecordingPasswordHasher implements PasswordHasher {

		private String rawPassword;

		private int invocations;

		@Override
		public String hash(String rawPassword) {
			invocations++;
			this.rawPassword = rawPassword;
			return "hashed-secret";
		}

	}

}
