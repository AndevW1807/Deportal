package com.deportal.domain.court;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public final class AvailableHours {

	private AvailableHours() {
	}

	public static BigDecimal between(LocalTime opensAt, LocalTime closesAt, LocalDate from, LocalDate to) {
		long days = ChronoUnit.DAYS.between(from, to) + 1;
		long minutes = Duration.between(opensAt, closesAt).toMinutes();
		return BigDecimal.valueOf(minutes)
			.multiply(BigDecimal.valueOf(days))
			.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
	}

	public static BigDecimal occupancy(BigDecimal reservedHours, BigDecimal availableHours) {
		if (reservedHours.signum() == 0 || availableHours.signum() == 0) {
			return BigDecimal.ZERO.setScale(2);
		}
		return reservedHours.multiply(BigDecimal.valueOf(100)).divide(availableHours, 2, RoundingMode.HALF_UP);
	}

}
