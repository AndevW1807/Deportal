package com.deportal.domain.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationRulesTest {

	@Test
	void confirmsAFreeSlotAndWaitsOnASameDayOverlap() {
		assertThat(ReservationPlacement.decide(true, false, false)).isEqualTo(ReservationDecision.CONFIRM);
		assertThat(ReservationPlacement.decide(true, true, true)).isEqualTo(ReservationDecision.WAIT);
		assertThat(ReservationPlacement.decide(false, true, true)).isEqualTo(ReservationDecision.REJECT);
		assertThat(ReservationPlacement.decide(true, false, true)).isEqualTo(ReservationDecision.REJECT);
	}

	@Test
	void promotesMembersFirstAndKeepsTheArrivalOrderInsideEachGroup() {
		Reservation olderMember = waiting(1L, 7L);
		Reservation olderGuest = waiting(2L, 8L);
		Reservation newerMember = waiting(3L, 9L);

		List<Reservation> ordered = WaitlistPriority.order(List.of(olderMember, olderGuest, newerMember),
				Set.of(7L, 9L));

		assertThat(ordered).extracting(Reservation::id).containsExactly(1L, 3L, 2L);
	}

	@Test
	void capsTheDiscountAtThirtyPercent() {
		assertThat(ReservationDiscount.rate(true)).isEqualByComparingTo("0.10");
		assertThat(ReservationDiscount.rate(false)).isEqualByComparingTo("0.00");
		assertThat(ReservationDiscount.rate(true)).isLessThanOrEqualTo(ReservationDiscount.MAX_RATE);
		assertThat(ReservationDiscount.total(new BigDecimal("20.00"), 2, new BigDecimal("0.10")))
			.isEqualByComparingTo("36.00");
	}

	private static Reservation waiting(Long id, Long customerId) {
		return new Reservation(id, customerId, 4L, LocalDate.of(2026, 10, 18), LocalTime.of(16, 0), 1,
				ReservationStatus.WAITING, new BigDecimal("20.00"), new BigDecimal("0.00"), new BigDecimal("20.00"));
	}

}
