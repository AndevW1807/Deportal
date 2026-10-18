package com.deportal.application.reservation;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.deportal.domain.court.Court;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.court.failure.CourtNotFound;
import com.deportal.domain.reservation.CourtWindow;
import com.deportal.domain.reservation.Reservation;
import com.deportal.domain.reservation.ReservationDecision;
import com.deportal.domain.reservation.ReservationDiscount;
import com.deportal.domain.reservation.ReservationPlacement;
import com.deportal.domain.reservation.ReservationRepository;
import com.deportal.domain.reservation.ReservationStatus;
import com.deportal.domain.reservation.ScheduleConflict;
import com.deportal.domain.reservation.failure.InvalidReservation;
import com.deportal.domain.reservation.failure.ReservationConflict;
import com.deportal.domain.user.CustomerNotFound;
import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserRepository;
import com.deportal.domain.user.UserRole;

public final class CreateReservation {

	private final CourtRepository courts;
	private final UserRepository users;
	private final ReservationRepository reservations;
	private final Clock clock;

	public CreateReservation(CourtRepository courts, UserRepository users, ReservationRepository reservations,
			Clock clock) {
		this.courts = courts;
		this.users = users;
		this.reservations = reservations;
		this.clock = clock;
	}

	public ReservationDetails execute(Long customerId, Long courtId, LocalDate date, LocalTime startsAt,
			int durationHours) {
		var customer = users.findActiveById(customerId)
			.filter(account -> account.role() == UserRole.CUSTOMER)
			.orElseThrow(CustomerNotFound::new);
		Court court = courts.findActiveById(courtId).orElseThrow(CourtNotFound::new);
		LocalDate today = LocalDate.now(clock.withZone(ListAvailability.BOGOTA));
		if (date == null || startsAt == null || date.isBefore(today)
				|| !CourtWindow.fits(court.opensAt(), court.closesAt(), startsAt, durationHours)) {
			throw new InvalidReservation();
		}
		if (date.equals(today) && !startsAt.isAfter(LocalTime.now(clock.withZone(ListAvailability.BOGOTA)))) {
			throw new InvalidReservation();
		}
		List<Reservation> confirmed = reservations.findConfirmed(courtId, date);
		boolean overlaps = confirmed.stream()
			.anyMatch(existing -> ScheduleConflict.overlaps(existing.startsAt(), existing.durationHours(), startsAt,
					durationHours));
		boolean blocked = confirmed.stream()
			.anyMatch(existing -> ScheduleConflict.conflicts(existing.startsAt(), existing.durationHours(), startsAt,
					durationHours));
		boolean member = customer.type() == MembershipType.MEMBER;
		ReservationDecision decision = ReservationPlacement.decide(date.equals(today), overlaps, blocked);
		if (decision == ReservationDecision.REJECT) {
			throw new ReservationConflict();
		}
		BigDecimal discount = ReservationDiscount.rate(member);
		BigDecimal total = ReservationDiscount.total(court.hourlyRate(), durationHours, discount);
		ReservationStatus status = decision == ReservationDecision.WAIT ? ReservationStatus.WAITING
				: ReservationStatus.CONFIRMED;
		Reservation saved = reservations.save(new Reservation(null, customerId, courtId, date, startsAt, durationHours,
				status, court.hourlyRate(), discount, total));
		return new ReservationDetails(saved, court.name());
	}

}
