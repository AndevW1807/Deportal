package com.deportal.infrastructure.persistence.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.deportal.domain.report.CourtReservationUse;
import com.deportal.domain.report.ReservationUsage;
import com.deportal.domain.reservation.ReservationStatus;

import org.springframework.stereotype.Repository;

@Repository
public class ReservationUsageAdapter implements ReservationUsage {

	private final ReservationJpaRepository reservations;

	public ReservationUsageAdapter(ReservationJpaRepository reservations) {
		this.reservations = reservations;
	}

	@Override
	public List<CourtReservationUse> between(LocalDate from, LocalDate to) {
		return aggregate(reservations.findByStatusAndDeletedAtIsNullAndReservationDateBetween(
				ReservationStatus.CONFIRMED, from, to));
	}

	static List<CourtReservationUse> aggregate(List<ReservationJpaEntity> rows) {
		Map<Long, List<ReservationJpaEntity>> byCourt = new LinkedHashMap<>();
		for (ReservationJpaEntity row : rows) {
			byCourt.computeIfAbsent(row.courtId(), id -> new ArrayList<>()).add(row);
		}
		List<CourtReservationUse> usage = new ArrayList<>();
		for (Map.Entry<Long, List<ReservationJpaEntity>> entry : byCourt.entrySet()) {
			BigDecimal hours = BigDecimal.ZERO.setScale(2);
			BigDecimal income = BigDecimal.ZERO.setScale(2);
			for (ReservationJpaEntity row : entry.getValue()) {
				hours = hours.add(reservedHours(row));
				income = income.add(row.amountPaid()).setScale(2, RoundingMode.HALF_UP);
			}
			usage.add(new CourtReservationUse(entry.getKey(), entry.getValue().size(), hours, income));
		}
		return usage;
	}

	private static BigDecimal reservedHours(ReservationJpaEntity row) {
		long minutes = ChronoUnit.MINUTES.between(row.startsAt(), row.endsAt());
		return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
	}

}
