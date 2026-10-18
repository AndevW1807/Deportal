package com.deportal.domain.user;

import java.util.Optional;

public interface UserRepository {

	Optional<UserAccount> findByEmail(String email);

	Optional<UserAccount> findActiveById(Long id);

	UserAccount save(UserAccount account);

}
