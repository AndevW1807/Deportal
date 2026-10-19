package com.deportal.application.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.reservation.RefundPolicy;
import com.deportal.domain.reservation.Reservation;
import com.deportal.domain.reservation.ReservationRepository;
import com.deportal.domain.reservation.ReservationStatus;

public final class ListMyReservations {

	private final ReservationRepository reservations;
	private final CourtRepository courts;
	private final Clock clock;

	public ListMyReservations(ReservationRepository reservations, CourtRepository courts, Clock clock) {
		this.reservations = reservations;
		this.courts = courts;
		this.clock = clock;
	}

	public List<OwnedReservation> execute(Long customerId) {
		Map<Long, String> names = courts.findActive()
			.stream()
			.collect(Collectors.toMap(court -> court.id(), court -> court.name()));
		return reservations.findActiveByCustomer(customerId)
			.stream()
			.map(reservation -> view(reservation, names.getOrDefault(reservation.courtId(), "Cancha"), clock))
			.toList();
	}

	private static OwnedReservation view(Reservation reservation, String courtName, Clock clock) {
		Duration untilStart = ReservationTime.untilStart(reservation, clock);
		boolean cancellable = !untilStart.isZero() && !untilStart.isNegative();
		BigDecimal refund = cancellable ? refundPreview(reservation, untilStart) : null;
		return new OwnedReservation(new ReservationDetails(reservation, courtName), cancellable, refund);
	}

	private static BigDecimal refundPreview(Reservation reservation, Duration untilStart) {
		if (reservation.status() == ReservationStatus.WAITING) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
		}
		return RefundPolicy.amount(reservation.total(), untilStart);
	}

}
