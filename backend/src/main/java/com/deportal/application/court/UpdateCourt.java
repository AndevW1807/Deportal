package com.deportal.application.court;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.deportal.domain.court.Court;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.court.SportType;
import com.deportal.domain.court.SportTypeCatalog;
import com.deportal.domain.court.failure.CourtNameAlreadyUsed;
import com.deportal.domain.court.failure.CourtNotFound;
import com.deportal.domain.court.failure.InvalidCourt;

public final class UpdateCourt {

	private final CourtRepository courts;
	private final SportTypeCatalog sports;

	public UpdateCourt(CourtRepository courts, SportTypeCatalog sports) {
		this.courts = courts;
		this.sports = sports;
	}

	public CourtDetails execute(Long id, String name, String sportTypeCode, int capacity, LocalTime opensAt,
			LocalTime closesAt, BigDecimal hourlyRate) {
		Court current = courts.findActiveById(id).orElseThrow(CourtNotFound::new);
		if (!sports.exists(sportTypeCode == null ? null : sportTypeCode.trim())) {
			throw new InvalidCourt();
		}
		Court revised = current.revise(name, sportTypeCode, capacity, opensAt, closesAt, hourlyRate);
		if (courts.existsActiveName(revised.name(), id)) {
			throw new CourtNameAlreadyUsed();
		}
		Court saved = courts.save(revised);
		SportType sport = sports.findActive()
			.stream()
			.filter(candidate -> candidate.code().equals(saved.sportTypeCode()))
			.findFirst()
			.orElseThrow(InvalidCourt::new);
		return new CourtDetails(saved, sport);
	}

}
