package com.deportal.application.auth;

import java.util.Optional;

import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserAccount;
import com.deportal.domain.user.UserRepository;
import com.deportal.domain.user.UserRole;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginTest {

	private static final String EMAIL = "juan.perez@deportal.test";

	private static final String HASH = "stored-hash";

	@Test
	void signsInWhenThePasswordMatches() {
		Login login = new Login(repository(activeUser()), matcher(true));

		AuthenticatedUser user = login.execute("  Juan.Perez@Deportal.Test ", "secret");

		assertThat(user.email()).isEqualTo(EMAIL);
		assertThat(user.role()).isEqualTo(UserRole.CUSTOMER);
		assertThat(user.type()).isEqualTo(MembershipType.MEMBER);
	}

	@Test
	void unknownWrongAndDeletedCredentialsAreIndistinguishable() {
		InvalidCredentials unknown = failure(repository(null), matcher(false), "missing@deportal.test");
		InvalidCredentials wrong = failure(repository(activeUser()), matcher(false), EMAIL);
		InvalidCredentials deleted = failure(repository(deletedUser()), matcher(true), EMAIL);

		assertThat(unknown.getMessage()).isEqualTo(InvalidCredentials.MESSAGE);
		assertThat(wrong.getMessage()).isEqualTo(unknown.getMessage());
		assertThat(deleted.getMessage()).isEqualTo(unknown.getMessage());
		assertThat(unknown).hasSameClassAs(wrong).hasSameClassAs(deleted);
	}

	@Test
	void checksAHashWhenTheEmailIsUnknown() {
		RecordingPasswordMatcher passwords = matcher(false);

		assertThatThrownBy(() -> new Login(repository(null), passwords).execute(EMAIL, "secret"))
			.isInstanceOf(InvalidCredentials.class);

		assertThat(passwords.lastHash).isEqualTo("dummy-hash");
	}

	@Test
	void rejectsPasswordsLongerThanTheBcryptLimitBeforeHashing() {
		RecordingPasswordMatcher passwords = matcher(true);

		assertThatThrownBy(() -> new Login(repository(activeUser()), passwords).execute(EMAIL, "a".repeat(73)))
			.isInstanceOf(PasswordTooLong.class);

		assertThat(passwords.invocations).isZero();
	}

	private static InvalidCredentials failure(UserRepository users, PasswordMatcher passwords, String email) {
		Throwable thrown = org.assertj.core.api.Assertions
			.catchThrowable(() -> new Login(users, passwords).execute(email, "secret"));
		assertThat(thrown).isInstanceOf(InvalidCredentials.class);
		return (InvalidCredentials) thrown;
	}

	private static UserRepository repository(UserAccount user) {
		return new UserRepository() {
			@Override
			public Optional<UserAccount> findByEmail(String email) {
				return Optional.ofNullable(user);
			}

			@Override
			public Optional<UserAccount> findActiveById(Long id) {
				return Optional.ofNullable(user);
			}
		};
	}

	private static RecordingPasswordMatcher matcher(boolean matches) {
		return new RecordingPasswordMatcher(matches);
	}

	private static UserAccount activeUser() {
		return new UserAccount(1L, "Juan Pérez", EMAIL, HASH, UserRole.CUSTOMER, MembershipType.MEMBER, false);
	}

	private static UserAccount deletedUser() {
		return new UserAccount(1L, "Juan Pérez", EMAIL, HASH, UserRole.CUSTOMER, MembershipType.MEMBER, true);
	}

	private static final class RecordingPasswordMatcher implements PasswordMatcher {

		private final boolean matches;

		private String lastHash;

		private int invocations;

		private RecordingPasswordMatcher(boolean matches) {
			this.matches = matches;
		}

		@Override
		public boolean matches(String rawPassword, String passwordHash) {
			invocations++;
			lastHash = passwordHash;
			return matches;
		}

		@Override
		public String dummyHash() {
			return "dummy-hash";
		}

	}

}
