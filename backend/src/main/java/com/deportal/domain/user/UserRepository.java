package com.deportal.domain.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

	Optional<UserAccount> findByEmail(String email);

	Optional<UserAccount> findActiveById(Long id);

	List<UserAccount> findActiveCustomers();

	UserAccount save(UserAccount account);

}
