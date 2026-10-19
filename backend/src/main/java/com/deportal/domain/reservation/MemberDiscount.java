package com.deportal.domain.reservation;

import java.math.BigDecimal;
import java.time.LocalTime;

public final class MemberDiscount implements DiscountStrategy {

	public static final BigDecimal RATE = new BigDecimal("0.10");

	@Override
	public DiscountKind kind() {
		return DiscountKind.MEMBER;
	}

	@Override
	public boolean applies(boolean member, LocalTime startsAt) {
		return member;
	}

	@Override
	public BigDecimal rate() {
		return RATE;
	}

}
