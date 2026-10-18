package com.deportal.infrastructure.web.report;

import java.math.BigDecimal;
import java.util.List;

import com.deportal.application.report.UtilizationReportView;

public record UtilizationReportResponse(int reservations, BigDecimal reservedHours, BigDecimal income,
		BigDecimal occupancyRate, List<CourtUtilizationResponse> courts) {

	static UtilizationReportResponse from(UtilizationReportView report) {
		return new UtilizationReportResponse(report.reservations(), report.reservedHours(), report.income(),
				report.occupancyRate(),
				report.courts()
					.stream()
					.map(court -> new CourtUtilizationResponse(court.id(), court.name(), court.sportName(),
							court.reservations(), court.reservedHours(), court.availableHours(), court.occupancyRate(),
							court.income()))
					.toList());
	}

}
