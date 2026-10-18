package com.deportal.application.reservation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.reservation.Reservation;
import com.deportal.domain.reservation.ReservationRepository;
import com.deportal.domain.reservation.ReservationStatus;
import com.deportal.domain.reservation.ScheduleConflict;
import com.deportal.domain.reservation.WaitlistPriority;
import com.deportal.domain.reservation.failure.ReservationNotFound;
import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserRepository;

public final class CancelReservation {

	private final ReservationRepository reservations;
	private final CourtRepository courts;
	private final UserRepository users;

	public CancelReservation(ReservationRepository reservations, CourtRepository courts, UserRepository users) {
		this.reservations = reservations;
		this.courts = courts;
		this.users = users;
	}

	public ReservationDetails execute(Long customerId, Long reservationId) {
		Reservation current = reservations.findActiveById(reservationId)
			.filter(reservation -> reservation.customerId().equals(customerId))
			.orElseThrow(ReservationNotFound::new);
		Reservation cancelled = current.withStatus(ReservationStatus.CANCELLED);
		if (current.status() != ReservationStatus.CONFIRMED) {
			return details(reservations.save(cancelled));
		}
		List<Reservation> changes = new ArrayList<>();
		changes.add(cancelled);
		List<Reservation> confirmed = new ArrayList<>(reservations.findConfirmed(current.courtId(), current.date())
			.stream()
			.filter(reservation -> !reservation.id().equals(current.id()))
			.toList());
		List<Reservation> waiting = reservations.findWaiting(current.courtId(), current.date());
		Set<Long> members = memberIds(waiting);
		WaitlistPriority.order(waiting, members)
			.stream()
			.filter(candidate -> fits(candidate, confirmed))
			.findFirst()
			.ifPresent(waitingReservation -> {
				Reservation promoted = waitingReservation.withStatus(ReservationStatus.CONFIRMED);
				confirmed.add(promoted);
				changes.add(promoted);
			});
		List<Reservation> saved = reservations.saveAll(changes);
		return details(saved.get(0));
	}

	private Set<Long> memberIds(List<Reservation> waiting) {
		Set<Long> members = new HashSet<>();
		for (Reservation reservation : waiting) {
			users.findActiveById(reservation.customerId())
				.filter(account -> account.type() == MembershipType.MEMBER)
				.ifPresent(account -> members.add(account.id()));
		}
		return members;
	}

	private ReservationDetails details(Reservation reservation) {
		String courtName = courts.findActiveById(reservation.courtId()).map(court -> court.name()).orElse("Cancha");
		return new ReservationDetails(reservation, courtName);
	}

	private static boolean fits(Reservation waiting, List<Reservation> confirmed) {
		return confirmed.stream()
			.noneMatch(existing -> ScheduleConflict.conflicts(existing.startsAt(), existing.durationHours(),
					waiting.startsAt(), waiting.durationHours()));
	}

}
