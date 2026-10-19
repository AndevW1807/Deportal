package com.deportal.application.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.deportal.domain.court.Court;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.court.failure.CourtNotFound;
import com.deportal.domain.reservation.CourtWindow;
import com.deportal.domain.reservation.Reservation;
import com.deportal.domain.reservation.ReservationRepository;
import com.deportal.domain.reservation.failure.InvalidReservation;

public final class ListAvailability {

	private final CourtRepository courts;
	private final ReservationRepository reservations;
	private final Clock clock;

	public ListAvailability(CourtRepository courts, ReservationRepository reservations, Clock clock) {
		this.courts = courts;
		this.reservations = reservations;
		this.clock = clock;
	}

	public List<AvailableStart> execute(Long courtId, LocalDate date, int durationHours) {
		Court court = courts.findActiveById(courtId).orElseThrow(CourtNotFound::new);
		LocalDate today = ReservationTime.today(clock);
		if (date == null || date.isBefore(today) || !CourtWindow.durationIsValid(durationHours)) {
			throw new InvalidReservation();
		}
		List<Reservation> confirmed = reservations.findConfirmed(courtId, date);
		LocalTime now = ReservationTime.now(clock);
		boolean sameDay = date.equals(today);
		return CourtWindow.starts(court.opensAt(), court.closesAt(), durationHours)
			.stream()
			.filter(start -> !sameDay || start.isAfter(now))
			.map(start -> AvailableStart.classify(start, durationHours, confirmed))
			.toList();
	}

}
