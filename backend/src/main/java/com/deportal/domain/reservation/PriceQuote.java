package com.deportal.domain.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PriceQuote(BigDecimal subtotal, BigDecimal memberDiscount, BigDecimal offPeakDiscount, BigDecimal total) {

	public BigDecimal discountAmount() {
		return subtotal.subtract(total).setScale(2, RoundingMode.HALF_UP);
	}

	public BigDecimal discountRate() {
		if (subtotal.signum() == 0) {
			return BigDecimal.ZERO.setScale(2);
		}
		return discountAmount().divide(subtotal, 2, RoundingMode.HALF_UP);
	}

}
