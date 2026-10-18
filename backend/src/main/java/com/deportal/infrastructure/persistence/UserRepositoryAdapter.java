package com.deportal.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.deportal.domain.user.EmailAlreadyUsed;
import com.deportal.domain.user.UserAccount;
import com.deportal.domain.user.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepository {

	private final UserJpaRepository users;

	public UserRepositoryAdapter(UserJpaRepository users) {
		this.users = users;
	}

	@Override
	public Optional<UserAccount> findByEmail(String email) {
		List<UserJpaEntity> rows = users.findByEmail(email);
		return rows.stream()
			.filter(row -> row.getDeletedAt() == null)
			.findFirst()
			.or(() -> rows.stream().findFirst())
			.map(this::toDomain);
	}

	@Override
	public Optional<UserAccount> findActiveById(Long id) {
		return users.findByIdAndDeletedAtIsNull(id).map(this::toDomain);
	}

	@Override
	public UserAccount save(UserAccount account) {
		try {
			UserJpaEntity saved = users.save(UserJpaEntity.newAccount(account.name(), account.email(),
					account.passwordHash(), account.role(), account.type()));
			return toDomain(saved);
		}
		catch (DataIntegrityViolationException exception) {
			throw new EmailAlreadyUsed();
		}
	}

	private UserAccount toDomain(UserJpaEntity row) {
		return new UserAccount(row.getId(), row.getName(), row.getEmail(), row.getPasswordHash(), row.getRole(),
				row.getType(), row.getDeletedAt() != null);
	}

}
