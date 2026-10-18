package com.deportal.infrastructure.persistence.court;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

public interface CourtJpaRepository extends Repository<CourtJpaEntity, Long> {

	List<CourtJpaEntity> findByDeletedAtIsNullOrderByNameAsc();

	Optional<CourtJpaEntity> findByIdAndDeletedAtIsNull(Long id);

	boolean existsByNameAndDeletedAtIsNull(String name);

	boolean existsByNameAndDeletedAtIsNullAndIdNot(String name, Long id);

	CourtJpaEntity save(CourtJpaEntity court);

}
