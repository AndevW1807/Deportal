package com.deportal.infrastructure.persistence.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.deportal.domain.reservation.Reservation;
import com.deportal.domain.reservation.ReservationRepository;
import com.deportal.domain.reservation.ReservationStatus;
import com.deportal.domain.reservation.failure.ReservationNotFound;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ReservationRepositoryAdapter implements ReservationRepository {

	private final ReservationJpaRepository reservations;
	private final Clock clock;

	public ReservationRepositoryAdapter(ReservationJpaRepository reservations, Clock clock) {
		this.reservations = reservations;
		this.clock = clock;
	}

	@Override
	public Reservation save(Reservation reservation) {
		return saveAll(List.of(reservation)).get(0);
	}

	@Override
	public List<Reservation> saveAll(List<Reservation> batch) {
		LocalDateTime now = LocalDateTime.now(clock);
		return batch.stream().map(reservation -> store(reservation, now)).toList();
	}

	@Override
	public List<Reservation> findConfirmed(Long courtId, LocalDate date) {
		return reservations
			.findByCourtIdAndReservationDateAndStatusAndDeletedAtIsNull(courtId, date, ReservationStatus.CONFIRMED)
			.stream()
			.map(row -> row.toDomain())
			.toList();
	}

	@Override
	public List<Reservation> findWaiting(Long courtId, LocalDate date) {
		return reservations
			.findByCourtIdAndReservationDateAndStatusAndDeletedAtIsNullOrderByCreatedAtAscIdAsc(courtId, date,
					ReservationStatus.WAITING)
			.stream()
			.map(row -> row.toDomain())
			.toList();
	}

	@Override
	public List<Reservation> findActiveByCustomer(Long customerId) {
		return reservations
			.findByUserIdAndStatusInAndDeletedAtIsNullOrderByReservationDateAscStartsAtAsc(customerId,
					List.of(ReservationStatus.CONFIRMED, ReservationStatus.WAITING))
			.stream()
			.map(row -> row.toDomain())
			.toList();
	}

	@Override
	public Optional<Reservation> findActiveById(Long id) {
		return reservations.findByIdAndDeletedAtIsNull(id)
			.filter(row -> row.toDomain().status() != ReservationStatus.CANCELLED)
			.map(row -> row.toDomain());
	}

	private Reservation store(Reservation reservation, LocalDateTime now) {
		if (reservation.id() == null) {
			return reservations.save(ReservationJpaEntity.create(reservation, now)).toDomain();
		}
		ReservationJpaEntity row = reservations.findByIdAndDeletedAtIsNull(reservation.id())
			.orElseThrow(ReservationNotFound::new);
		row.changeStatus(reservation.status(), now);
		return reservations.save(row).toDomain();
	}

}
