package com.deportal.domain.reservation;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourtWindowTest {

	private static final LocalTime OPENS = LocalTime.of(6, 0);
	private static final LocalTime CLOSES = LocalTime.of(22, 0);

	@Test
	void acceptsDurationsFromOneToEightHours() {
		assertThat(CourtWindow.durationIsValid(1)).isTrue();
		assertThat(CourtWindow.durationIsValid(8)).isTrue();
		assertThat(CourtWindow.durationIsValid(0)).isFalse();
		assertThat(CourtWindow.durationIsValid(9)).isFalse();
	}

	@Test
	void requiresTheReservationToFitInsideTheCourtHours() {
		assertThat(CourtWindow.fits(OPENS, CLOSES, LocalTime.of(6, 0), 8)).isTrue();
		assertThat(CourtWindow.fits(OPENS, CLOSES, LocalTime.of(14, 0), 8)).isTrue();
		assertThat(CourtWindow.fits(OPENS, CLOSES, LocalTime.of(15, 0), 8)).isFalse();
		assertThat(CourtWindow.fits(OPENS, CLOSES, LocalTime.of(5, 0), 1)).isFalse();
	}

}
