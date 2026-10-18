package com.deportal.infrastructure.persistence.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

public interface UserJpaRepository extends Repository<UserJpaEntity, Long> {

	List<UserJpaEntity> findByEmail(String email);

	Optional<UserJpaEntity> findByIdAndDeletedAtIsNull(Long id);

	UserJpaEntity save(UserJpaEntity account);

}
