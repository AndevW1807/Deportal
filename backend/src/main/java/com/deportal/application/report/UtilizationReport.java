package com.deportal.application.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.deportal.domain.court.AvailableHours;
import com.deportal.domain.court.Court;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.report.CourtReservationUse;
import com.deportal.domain.report.ReservationUsage;
import com.deportal.domain.court.SportType;
import com.deportal.domain.court.SportTypeCatalog;
import com.deportal.domain.report.failure.InvalidReportPeriod;

public final class UtilizationReport {

	private final CourtRepository courts;
	private final SportTypeCatalog sports;
	private final ReservationUsage usage;

	public UtilizationReport(CourtRepository courts, SportTypeCatalog sports, ReservationUsage usage) {
		this.courts = courts;
		this.sports = sports;
		this.usage = usage;
	}

	public UtilizationReportView execute(LocalDate from, LocalDate to, String sportTypeCode) {
		if (from == null || to == null || to.isBefore(from)) {
			throw new InvalidReportPeriod();
		}
		String sport = blankToNull(sportTypeCode);
		Map<String, SportType> byCode = sports.findActive()
			.stream()
			.collect(Collectors.toMap(item -> item.code(), item -> item));
		Map<Long, CourtReservationUse> used = usage.between(from, to)
			.stream()
			.collect(Collectors.toMap(item -> item.courtId(), item -> item, (left, right) -> left));
		List<CourtUtilization> rows = courts.findActive()
			.stream()
			.filter(court -> sport == null || court.sportTypeCode().equals(sport))
			.map(court -> row(court, byCode, used.get(court.id()), from, to))
			.sorted(Comparator.comparing(court -> court.name(), String.CASE_INSENSITIVE_ORDER))
			.toList();
		int reservations = rows.stream().mapToInt(court -> court.reservations()).sum();
		BigDecimal reserved = rows.stream()
			.map(court -> court.reservedHours())
			.reduce(BigDecimal.ZERO, (total, hours) -> total.add(hours));
		BigDecimal available = rows.stream()
			.map(court -> court.availableHours())
			.reduce(BigDecimal.ZERO, (total, hours) -> total.add(hours));
		BigDecimal income = rows.stream()
			.map(court -> court.income())
			.reduce(BigDecimal.ZERO, (total, amount) -> total.add(amount));
		return new UtilizationReportView(reservations, reserved, income, AvailableHours.occupancy(reserved, available),
				rows);
	}

	private static CourtUtilization row(Court court, Map<String, SportType> byCode, CourtReservationUse use,
			LocalDate from, LocalDate to) {
		BigDecimal available = AvailableHours.between(court.opensAt(), court.closesAt(), from, to);
		int reservations = use == null ? 0 : use.reservations();
		BigDecimal reserved = use == null ? BigDecimal.ZERO.setScale(2) : use.reservedHours();
		BigDecimal income = use == null ? BigDecimal.ZERO.setScale(2) : use.income();
		SportType sport = byCode.get(court.sportTypeCode());
		String sportName = sport == null ? court.sportTypeCode() : sport.name();
		return new CourtUtilization(court.id(), court.name(), sportName, reservations, reserved, available,
				AvailableHours.occupancy(reserved, available), income);
	}

	private static String blankToNull(String sportTypeCode) {
		if (sportTypeCode == null || sportTypeCode.isBlank()) {
			return null;
		}
		return sportTypeCode.trim();
	}

}
