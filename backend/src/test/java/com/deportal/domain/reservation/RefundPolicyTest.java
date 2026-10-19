package com.deportal.domain.reservation;

import java.math.BigDecimal;
import java.time.Duration;

import com.deportal.domain.reservation.failure.InvalidReservation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefundPolicyTest {

	private static final BigDecimal PAID = new BigDecimal("20.00");

	@Test
	void refundsTheFullAmountMoreThanADayAhead() {
		assertThat(RefundPolicy.amount(PAID, Duration.ofHours(25))).isEqualByComparingTo("20.00");
	}

	@Test
	void refundsHalfBetweenTwoAndTwentyFourHours() {
		assertThat(RefundPolicy.amount(PAID, Duration.ofHours(24))).isEqualByComparingTo("10.00");
		assertThat(RefundPolicy.amount(PAID, Duration.ofHours(2))).isEqualByComparingTo("10.00");
	}

	@Test
	void refundsNothingInsideTheLastTwoHours() {
		assertThat(RefundPolicy.amount(PAID, Duration.ofHours(2).minusMinutes(1))).isEqualByComparingTo("0.00");
	}

	@Test
	void refusesAReservationThatAlreadyStarted() {
		assertThatThrownBy(() -> RefundPolicy.amount(PAID, Duration.ZERO)).isInstanceOf(InvalidReservation.class);
		assertThatThrownBy(() -> RefundPolicy.amount(PAID, Duration.ofMinutes(-1))).isInstanceOf(InvalidReservation.class);
	}

}
