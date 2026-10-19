package com.deportal.application.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.deportal.domain.court.Court;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.court.failure.CourtNotFound;
import com.deportal.domain.reservation.CourtWindow;
import com.deportal.domain.reservation.PriceQuote;
import com.deportal.domain.reservation.Reservation;
import com.deportal.domain.reservation.ReservationDecision;
import com.deportal.domain.reservation.ReservationPricing;
import com.deportal.domain.reservation.ReservationPlacement;
import com.deportal.domain.reservation.ReservationRepository;
import com.deportal.domain.reservation.ReservationStatus;
import com.deportal.domain.reservation.SlotPressure;
import com.deportal.domain.reservation.failure.InvalidReservation;
import com.deportal.domain.reservation.failure.ReservationConflict;
import com.deportal.domain.user.failure.CustomerNotFound;
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
		LocalDate today = ReservationTime.today(clock);
		if (date == null || startsAt == null || date.isBefore(today)
				|| !CourtWindow.fits(court.opensAt(), court.closesAt(), startsAt, durationHours)) {
			throw new InvalidReservation();
		}
		if (date.equals(today) && !startsAt.isAfter(ReservationTime.now(clock))) {
			throw new InvalidReservation();
		}
		List<Reservation> confirmed = reservations.findConfirmed(courtId, date);
		SlotPressure pressure = SlotPressure.between(confirmed, startsAt, durationHours);
		boolean member = customer.type() == MembershipType.MEMBER;
		ReservationDecision decision = ReservationPlacement.decide(date.equals(today), pressure.overlaps(),
				pressure.blockedByCleaning());
		if (decision == ReservationDecision.REJECT) {
			throw new ReservationConflict();
		}
		PriceQuote price = ReservationPricing.quote(court.hourlyRate(), durationHours, member, startsAt);
		ReservationStatus status = decision == ReservationDecision.WAIT ? ReservationStatus.WAITING
				: ReservationStatus.CONFIRMED;
		Reservation saved = reservations.save(new Reservation(null, customerId, courtId, date, startsAt, durationHours,
				status, court.hourlyRate(), price.discountRate(), price.total(), null));
		return new ReservationDetails(saved, court.name());
	}

}
