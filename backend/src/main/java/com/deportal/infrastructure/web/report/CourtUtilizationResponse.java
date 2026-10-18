package com.deportal.infrastructure.web.report;

import java.math.BigDecimal;

public record CourtUtilizationResponse(Long id, String name, String sportName, int reservations,
		BigDecimal reservedHours, BigDecimal availableHours, BigDecimal occupancyRate, BigDecimal income) {
}
