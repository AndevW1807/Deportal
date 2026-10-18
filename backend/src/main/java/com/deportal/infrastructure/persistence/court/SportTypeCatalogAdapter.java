package com.deportal.infrastructure.persistence.court;

import java.util.List;

import com.deportal.domain.court.SportType;
import com.deportal.domain.court.SportTypeCatalog;

import org.springframework.stereotype.Repository;

@Repository
public class SportTypeCatalogAdapter implements SportTypeCatalog {

	private final SportTypeJpaRepository sports;

	public SportTypeCatalogAdapter(SportTypeJpaRepository sports) {
		this.sports = sports;
	}

	@Override
	public List<SportType> findActive() {
		return sports.findByDeletedAtIsNullOrderByNameAsc()
			.stream()
			.map(row -> new SportType(row.getCode(), row.getName()))
			.toList();
	}

	@Override
	public boolean exists(String code) {
		return code != null && sports.existsByCodeAndDeletedAtIsNull(code);
	}

}
