package com.deportal.infrastructure.persistence.court;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.deportal.domain.court.Court;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.court.failure.CourtNameAlreadyUsed;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class CourtRepositoryAdapter implements CourtRepository {

	private final CourtJpaRepository courts;
	private final Clock clock;

	public CourtRepositoryAdapter(CourtJpaRepository courts, Clock clock) {
		this.courts = courts;
		this.clock = clock;
	}

	@Override
	public List<Court> findActive() {
		return courts.findByDeletedAtIsNullOrderByNameAsc().stream().map(row -> row.toDomain()).toList();
	}

	@Override
	public Optional<Court> findActiveById(Long id) {
		return courts.findByIdAndDeletedAtIsNull(id).map(row -> row.toDomain());
	}

	@Override
	public boolean existsActiveName(String name, Long excludingId) {
		return courts.existsByNameAndDeletedAtIsNullAndIdNot(name, excludingId);
	}

	@Override
	public Court save(Court court) {
		try {
			return courts.save(CourtJpaEntity.from(court)).toDomain();
		}
		catch (DataIntegrityViolationException exception) {
			throw new CourtNameAlreadyUsed();
		}
	}

	@Override
	public void softDelete(Long id) {
		courts.findByIdAndDeletedAtIsNull(id)
			.ifPresent(row -> {
				row.markDeleted(LocalDateTime.now(clock));
				courts.save(row);
			});
	}

}
