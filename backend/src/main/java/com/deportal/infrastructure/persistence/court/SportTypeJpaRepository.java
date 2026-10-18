package com.deportal.infrastructure.persistence.court;

import java.util.List;

import org.springframework.data.repository.Repository;

public interface SportTypeJpaRepository extends Repository<SportTypeJpaEntity, String> {

	List<SportTypeJpaEntity> findByDeletedAtIsNullOrderByNameAsc();

	boolean existsByCodeAndDeletedAtIsNull(String code);

}
