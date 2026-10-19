package com.deportal.infrastructure.web.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;

import com.deportal.domain.reservation.PriceQuote;

public record QuoteResponse(String subtotal, String memberDiscount, String offPeakDiscount, String total,
		String endsAt) {

	static QuoteResponse from(PriceQuote quote, LocalTime startsAt, int durationHours) {
		return new QuoteResponse(money(quote.subtotal()), money(quote.memberDiscount()), money(quote.offPeakDiscount()),
				money(quote.total()), startsAt.plusHours(durationHours).toString());
	}

	private static String money(BigDecimal amount) {
		return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}

}
