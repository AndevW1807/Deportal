package com.deportal.domain.court;

import java.util.List;
import java.util.Optional;

public interface CourtRepository {

	List<Court> findActive();

	Optional<Court> findActiveById(Long id);

	boolean existsActiveName(String name, Long excludingId);

	Court save(Court court);

	void softDelete(Long id);

}
