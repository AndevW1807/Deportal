package com.deportal.application.report;

import java.math.BigDecimal;

public record CourtUtilization(Long id, String name, String sportName, int reservations, BigDecimal reservedHours,
		BigDecimal availableHours, BigDecimal occupancyRate, BigDecimal income) {
}
