package com.deportal.domain.reservation;

import java.math.BigDecimal;
import java.time.LocalTime;

public final class OffPeakDiscount implements DiscountStrategy {

	public static final BigDecimal RATE = new BigDecimal("0.20");
	public static final LocalTime MORNING_LIMIT = LocalTime.of(10, 0);
	public static final LocalTime EVENING_START = LocalTime.of(19, 0);

	@Override
	public DiscountKind kind() {
		return DiscountKind.OFF_PEAK;
	}

	@Override
	public boolean applies(boolean member, LocalTime startsAt) {
		return startsAt.isBefore(MORNING_LIMIT) || !startsAt.isBefore(EVENING_START);
	}

	@Override
	public BigDecimal rate() {
		return RATE;
	}

}
