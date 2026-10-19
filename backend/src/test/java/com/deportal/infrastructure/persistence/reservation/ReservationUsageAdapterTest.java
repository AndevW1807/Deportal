package com.deportal.infrastructure.persistence.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.deportal.domain.report.CourtReservationUse;
import com.deportal.domain.reservation.Reservation;
import com.deportal.domain.reservation.ReservationStatus;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationUsageAdapterTest {

	private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 18, 12, 0);
	private static final LocalDate FROM = LocalDate.of(2026, 10, 1);
	private static final LocalDate TO = LocalDate.of(2026, 10, 31);

	@Test
	void sumsConfirmedReservationsByCourt() {
		List<CourtReservationUse> usage = ReservationUsageAdapter.aggregate(List.of(
				row(4L, LocalTime.of(8, 0), 2, "28.80"),
				row(4L, LocalTime.of(16, 0), 1, "18.00"),
				row(5L, LocalTime.of(10, 0), 3, "60.00")));

		assertThat(usage).containsExactly(
				new CourtReservationUse(4L, 2, new BigDecimal("3.00"), new BigDecimal("46.80")),
				new CourtReservationUse(5L, 1, new BigDecimal("3.00"), new BigDecimal("60.00")));
	}

	@Test
	void asksOnlyForConfirmedReservationsInsideTheRange() {
		ReservationJpaEntity stored = row(4L, LocalTime.of(8, 0), 2, "28.80");
		ReservationJpaRepository repository = new ReservationJpaRepository() {

			@Override
			public ReservationJpaEntity save(ReservationJpaEntity reservation) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Optional<ReservationJpaEntity> findByIdAndDeletedAtIsNull(Long id) {
				throw new UnsupportedOperationException();
			}

			@Override
			public List<ReservationJpaEntity> findByCourtIdAndReservationDateAndStatusAndDeletedAtIsNull(Long courtId,
					LocalDate reservationDate, ReservationStatus status) {
				throw new UnsupportedOperationException();
			}

			@Override
			public List<ReservationJpaEntity>
					findByCourtIdAndReservationDateAndStatusAndDeletedAtIsNullOrderByCreatedAtAscIdAsc(Long courtId,
							LocalDate reservationDate, ReservationStatus status) {
				throw new UnsupportedOperationException();
			}

			@Override
			public List<ReservationJpaEntity> findByUserIdAndStatusInAndDeletedAtIsNullOrderByReservationDateAscStartsAtAsc(
					Long userId, Collection<ReservationStatus> statuses) {
				throw new UnsupportedOperationException();
			}

			@Override
			public List<ReservationJpaEntity> findByStatusAndDeletedAtIsNullAndReservationDateBetween(
					ReservationStatus status, LocalDate from, LocalDate to) {
				assertThat(status).isEqualTo(ReservationStatus.CONFIRMED);
				assertThat(from).isEqualTo(FROM);
				assertThat(to).isEqualTo(TO);
				return List.of(stored);
			}

		};

		assertThat(new ReservationUsageAdapter(repository).between(FROM, TO)).containsExactly(
				new CourtReservationUse(4L, 1, new BigDecimal("2.00"), new BigDecimal("28.80")));
	}

	private static ReservationJpaEntity row(Long courtId, LocalTime startsAt, int hours, String paid) {
		return ReservationJpaEntity.create(new Reservation(null, 7L, courtId, FROM, startsAt, hours,
				ReservationStatus.CONFIRMED, new BigDecimal("20.00"), BigDecimal.ZERO, new BigDecimal(paid), null), NOW);
	}

}
