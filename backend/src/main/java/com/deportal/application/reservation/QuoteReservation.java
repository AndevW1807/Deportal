package com.deportal.application.reservation;

import java.time.LocalTime;

import com.deportal.domain.court.Court;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.court.failure.CourtNotFound;
import com.deportal.domain.reservation.CourtWindow;
import com.deportal.domain.reservation.PriceQuote;
import com.deportal.domain.reservation.ReservationPricing;
import com.deportal.domain.reservation.failure.InvalidReservation;
import com.deportal.domain.user.failure.CustomerNotFound;
import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserRepository;
import com.deportal.domain.user.UserRole;

public final class QuoteReservation {

	private final CourtRepository courts;
	private final UserRepository users;

	public QuoteReservation(CourtRepository courts, UserRepository users) {
		this.courts = courts;
		this.users = users;
	}

	public PriceQuote execute(Long customerId, Long courtId, LocalTime startsAt, int durationHours) {
		var customer = users.findActiveById(customerId)
			.filter(account -> account.role() == UserRole.CUSTOMER)
			.orElseThrow(CustomerNotFound::new);
		Court court = courts.findActiveById(courtId).orElseThrow(CourtNotFound::new);
		if (startsAt == null || !CourtWindow.fits(court.opensAt(), court.closesAt(), startsAt, durationHours)) {
			throw new InvalidReservation();
		}
		return ReservationPricing.quote(court.hourlyRate(), durationHours, customer.type() == MembershipType.MEMBER,
				startsAt);
	}

}
