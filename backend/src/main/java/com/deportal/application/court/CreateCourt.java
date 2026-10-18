package com.deportal.application.court;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.deportal.domain.court.Court;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.court.SportType;
import com.deportal.domain.court.SportTypeCatalog;
import com.deportal.domain.court.failure.CourtNameAlreadyUsed;
import com.deportal.domain.court.failure.InvalidCourt;

public final class CreateCourt {

	private final CourtRepository courts;
	private final SportTypeCatalog sports;

	public CreateCourt(CourtRepository courts, SportTypeCatalog sports) {
		this.courts = courts;
		this.sports = sports;
	}

	public CourtDetails execute(String name, String sportTypeCode, int capacity, LocalTime opensAt, LocalTime closesAt,
			BigDecimal hourlyRate) {
		String code = sportTypeCode == null ? null : sportTypeCode.trim();
		if (!sports.exists(code)) {
			throw new InvalidCourt();
		}
		Court court = new Court(null, name, code, capacity, opensAt, closesAt, hourlyRate, false);
		if (courts.existsActiveName(court.name(), null)) {
			throw new CourtNameAlreadyUsed();
		}
		Court saved = courts.save(court);
		SportType sport = sports.findActive()
			.stream()
			.filter(candidate -> candidate.code().equals(saved.sportTypeCode()))
			.findFirst()
			.orElseThrow(InvalidCourt::new);
		return new CourtDetails(saved, sport);
	}

}
