package com.deportal.domain.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;

public final class ReservationPricing {

	public static final BigDecimal MAX_RATE = new BigDecimal("0.30");

	private static final List<DiscountStrategy> STRATEGIES = List.of(new MemberDiscount(), new OffPeakDiscount());

	private ReservationPricing() {
	}

	public static PriceQuote quote(BigDecimal hourlyRate, int hours, boolean member, LocalTime startsAt) {
		return quote(hourlyRate, hours, member, startsAt, STRATEGIES);
	}

	static PriceQuote quote(BigDecimal hourlyRate, int hours, boolean member, LocalTime startsAt,
			List<DiscountStrategy> strategies) {
		BigDecimal subtotal = hourlyRate.multiply(BigDecimal.valueOf(hours)).setScale(2, RoundingMode.HALF_UP);
		BigDecimal ceiling = subtotal.multiply(MAX_RATE).setScale(2, RoundingMode.HALF_UP);
		BigDecimal remaining = subtotal;
		BigDecimal consumed = BigDecimal.ZERO.setScale(2);
		BigDecimal memberDiscount = BigDecimal.ZERO.setScale(2);
		BigDecimal offPeakDiscount = BigDecimal.ZERO.setScale(2);
		for (DiscountStrategy strategy : strategies) {
			if (!strategy.applies(member, startsAt) || consumed.compareTo(ceiling) >= 0) {
				continue;
			}
			BigDecimal amount = remaining.multiply(strategy.rate()).setScale(2, RoundingMode.HALF_UP);
			BigDecimal room = ceiling.subtract(consumed);
			if (amount.compareTo(room) > 0) {
				amount = room;
			}
			if (strategy.kind() == DiscountKind.MEMBER) {
				memberDiscount = memberDiscount.add(amount);
			}
			else if (strategy.kind() == DiscountKind.OFF_PEAK) {
				offPeakDiscount = offPeakDiscount.add(amount);
			}
			remaining = remaining.subtract(amount).setScale(2, RoundingMode.HALF_UP);
			consumed = consumed.add(amount).setScale(2, RoundingMode.HALF_UP);
		}
		return new PriceQuote(subtotal, memberDiscount, offPeakDiscount, remaining);
	}

}
