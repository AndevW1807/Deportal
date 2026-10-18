package com.deportal.application.court;

import java.time.Clock;
import java.time.LocalDate;

import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.court.UpcomingReservations;
import com.deportal.domain.court.failure.CourtHasUpcomingReservations;
import com.deportal.domain.court.failure.CourtNotFound;

public final class DeleteCourt {

	private final CourtRepository courts;
	private final UpcomingReservations upcoming;
	private final Clock clock;

	public DeleteCourt(CourtRepository courts, UpcomingReservations upcoming, Clock clock) {
		this.courts = courts;
		this.upcoming = upcoming;
		this.clock = clock;
	}

	public void execute(Long id) {
		if (courts.findActiveById(id).isEmpty()) {
			throw new CourtNotFound();
		}
		if (upcoming.existsForCourt(id, LocalDate.now(clock))) {
			throw new CourtHasUpcomingReservations();
		}
		courts.softDelete(id);
	}

}
