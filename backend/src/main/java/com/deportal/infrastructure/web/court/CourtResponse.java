package com.deportal.infrastructure.web.court;

import java.time.format.DateTimeFormatter;

import com.deportal.application.court.CourtDetails;
import com.deportal.domain.court.Court;

public record CourtResponse(Long id, String name, SportTypeResponse sportType, int capacity, String opensAt,
		String closesAt, String hourlyRate) {

	private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH:mm");

	static CourtResponse from(CourtDetails details) {
		Court court = details.court();
		return new CourtResponse(court.id(), court.name(),
				new SportTypeResponse(details.sportType().code(), details.sportType().name()), court.capacity(),
				HOUR.format(court.opensAt()), HOUR.format(court.closesAt()), court.hourlyRate().toPlainString());
	}

}
