package com.deportal.infrastructure.persistence.reservation;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.deportal.domain.reservation.ReservationStatus;

import org.springframework.data.repository.Repository;

public interface ReservationJpaRepository extends Repository<ReservationJpaEntity, Long> {

	ReservationJpaEntity save(ReservationJpaEntity reservation);

	Optional<ReservationJpaEntity> findByIdAndDeletedAtIsNull(Long id);

	List<ReservationJpaEntity> findByCourtIdAndReservationDateAndStatusAndDeletedAtIsNull(Long courtId,
			LocalDate reservationDate, ReservationStatus status);

	List<ReservationJpaEntity> findByCourtIdAndReservationDateAndStatusAndDeletedAtIsNullOrderByCreatedAtAscIdAsc(
			Long courtId, LocalDate reservationDate, ReservationStatus status);

	List<ReservationJpaEntity> findByUserIdAndStatusInAndDeletedAtIsNullOrderByReservationDateAscStartsAtAsc(
			Long userId, Collection<ReservationStatus> statuses);

}
