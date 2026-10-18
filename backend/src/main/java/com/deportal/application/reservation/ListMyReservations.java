package com.deportal.application.reservation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.reservation.ReservationRepository;

public final class ListMyReservations {

	private final ReservationRepository reservations;
	private final CourtRepository courts;

	public ListMyReservations(ReservationRepository reservations, CourtRepository courts) {
		this.reservations = reservations;
		this.courts = courts;
	}

	public List<ReservationDetails> execute(Long customerId) {
		Map<Long, String> names = courts.findActive()
			.stream()
			.collect(Collectors.toMap(court -> court.id(), court -> court.name()));
		return reservations.findActiveByCustomer(customerId)
			.stream()
			.map(reservation -> new ReservationDetails(reservation, names.getOrDefault(reservation.courtId(), "Cancha")))
			.toList();
	}

}
