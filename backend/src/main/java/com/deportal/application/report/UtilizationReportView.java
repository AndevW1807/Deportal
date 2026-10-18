package com.deportal.application.report;

import java.math.BigDecimal;
import java.util.List;

public record UtilizationReportView(int reservations, BigDecimal reservedHours, BigDecimal income,
		BigDecimal occupancyRate, List<CourtUtilization> courts) {
}
