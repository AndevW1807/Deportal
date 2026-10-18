package com.deportal.domain.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ReservationDiscount {

	public static final BigDecimal MEMBER_RATE = new BigDecimal("0.10");
	public static final BigDecimal OFF_PEAK_RATE = BigDecimal.ZERO.setScale(2);
	public static final BigDecimal MAX_RATE = new BigDecimal("0.30");

	private ReservationDiscount() {
	}

	public static BigDecimal rate(boolean member) {
		BigDecimal rate = (member ? MEMBER_RATE : BigDecimal.ZERO).add(OFF_PEAK_RATE);
		if (rate.compareTo(MAX_RATE) > 0) {
			return MAX_RATE;
		}
		return rate.setScale(2, RoundingMode.HALF_UP);
	}

	public static BigDecimal total(BigDecimal hourlyRate, int hours, BigDecimal discountRate) {
		BigDecimal subtotal = hourlyRate.multiply(BigDecimal.valueOf(hours));
		BigDecimal discount = subtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
		return subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
	}

}
