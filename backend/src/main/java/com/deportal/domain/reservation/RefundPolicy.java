package com.deportal.domain.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

import com.deportal.domain.reservation.failure.InvalidReservation;

public final class RefundPolicy {

	private RefundPolicy() {
	}

	public static BigDecimal amount(BigDecimal paid, Duration untilStart) {
		if (paid == null || untilStart == null || untilStart.isZero() || untilStart.isNegative()) {
			throw new InvalidReservation();
		}
		if (untilStart.compareTo(Duration.ofHours(24)) > 0) {
			return paid.setScale(2, RoundingMode.HALF_UP);
		}
		if (untilStart.compareTo(Duration.ofHours(2)) >= 0) {
			return paid.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
		}
		return BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
	}

}
