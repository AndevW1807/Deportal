package com.deportal.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.deportal.domain.user.EmailAlreadyUsed;
import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserAccount;
import com.deportal.domain.user.UserRole;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRepositoryAdapterTest {

	@Test
	void aDuplicateActiveEmailFromTheDatabaseIsTheSameConflict() {
		UserJpaRepository users = new UserJpaRepository() {
			@Override
			public List<UserJpaEntity> findByEmail(String email) {
				return List.of();
			}

			@Override
			public Optional<UserJpaEntity> findByIdAndDeletedAtIsNull(Long id) {
				return Optional.empty();
			}

			@Override
			public UserJpaEntity save(UserJpaEntity account) {
				throw new DataIntegrityViolationException("duplicate active email");
			}
		};

		UserAccount account = new UserAccount(null, "Ana", "ana@deportal.test", "hashed", UserRole.CUSTOMER,
				MembershipType.NON_MEMBER, false);

		assertThatThrownBy(() -> new UserRepositoryAdapter(users).save(account)).isInstanceOf(EmailAlreadyUsed.class);
	}

}
