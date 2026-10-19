package com.deportal.domain.reservation;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationPricingTest {

	private static final BigDecimal FORTY = new BigDecimal("40.00");

	@Test
	void appliesMemberThenOffPeakInSequence() {
		PriceQuote quote = ReservationPricing.quote(FORTY, 1, true, LocalTime.of(8, 0));

		assertThat(quote.subtotal()).isEqualByComparingTo("40.00");
		assertThat(quote.memberDiscount()).isEqualByComparingTo("4.00");
		assertThat(quote.offPeakDiscount()).isEqualByComparingTo("7.20");
		assertThat(quote.total()).isEqualByComparingTo("28.80");
		assertThat(quote.discountRate()).isEqualByComparingTo("0.28");
	}

	@Test
	void appliesOnlyTheOffPeakDiscountToAGuest() {
		PriceQuote quote = ReservationPricing.quote(FORTY, 1, false, LocalTime.of(8, 0));

		assertThat(quote.memberDiscount()).isEqualByComparingTo("0.00");
		assertThat(quote.offPeakDiscount()).isEqualByComparingTo("8.00");
		assertThat(quote.total()).isEqualByComparingTo("32.00");
	}

	@Test
	void treatsTheMorningAndEveningEdgesAsOffPeak() {
		assertThat(offPeak(LocalTime.of(9, 0))).isTrue();
		assertThat(offPeak(LocalTime.of(10, 0))).isFalse();
		assertThat(offPeak(LocalTime.of(18, 0))).isFalse();
		assertThat(offPeak(LocalTime.of(19, 0))).isTrue();
	}

	@Test
	void stopsTheDiscountAtThirtyPercentOfTheSubtotal() {
		PriceQuote quote = ReservationPricing.quote(new BigDecimal("100.00"), 1, true, LocalTime.of(8, 0),
				List.of(new MemberDiscount(), new FixedRate(DiscountKind.OFF_PEAK, new BigDecimal("0.50"))));

		assertThat(quote.memberDiscount()).isEqualByComparingTo("10.00");
		assertThat(quote.offPeakDiscount()).isEqualByComparingTo("20.00");
		assertThat(quote.discountAmount()).isEqualByComparingTo("30.00");
		assertThat(quote.total()).isEqualByComparingTo("70.00");
	}

	private static boolean offPeak(LocalTime startsAt) {
		return ReservationPricing.quote(FORTY, 1, false, startsAt).offPeakDiscount().signum() > 0;
	}

	private record FixedRate(DiscountKind kind, BigDecimal rate) implements DiscountStrategy {

		@Override
		public boolean applies(boolean member, LocalTime startsAt) {
			return true;
		}

	}

}
