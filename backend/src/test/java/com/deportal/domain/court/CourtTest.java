package com.deportal.domain.court;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.deportal.domain.court.failure.InvalidCourt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourtTest {

	@Test
	void trimsTheNameAndKeepsAValidCourt() {
		Court court = sample("  Cancha Norte  ", 10, "06:00", "22:00", "20.00");

		assertThat(court.name()).isEqualTo("Cancha Norte");
		assertThat(court.hourlyRate()).isEqualByComparingTo("20.00");
		assertThat(court.deleted()).isFalse();
	}

	@Test
	void rejectsABlankName() {
		assertThatThrownBy(() -> sample("   ", 10, "06:00", "22:00", "20.00")).isInstanceOf(InvalidCourt.class);
	}

	@Test
	void rejectsANameLongerThan120Characters() {
		assertThatThrownBy(() -> sample("n".repeat(121), 10, "06:00", "22:00", "20.00"))
			.isInstanceOf(InvalidCourt.class);
	}

	@Test
	void rejectsCapacityOutsideOneToFifty() {
		assertThatThrownBy(() -> sample("Norte", 0, "06:00", "22:00", "20.00")).isInstanceOf(InvalidCourt.class);
		assertThatThrownBy(() -> sample("Norte", 51, "06:00", "22:00", "20.00")).isInstanceOf(InvalidCourt.class);
	}

	@Test
	void rejectsAnOpeningThatIsNotBeforeTheClosing() {
		assertThatThrownBy(() -> sample("Norte", 10, "18:00", "18:00", "20.00")).isInstanceOf(InvalidCourt.class);
		assertThatThrownBy(() -> sample("Norte", 10, "19:00", "18:00", "20.00")).isInstanceOf(InvalidCourt.class);
	}

	@Test
	void rejectsHoursOutsideTheGlobalWindow() {
		assertThatThrownBy(() -> sample("Norte", 10, "05:59", "22:00", "20.00")).isInstanceOf(InvalidCourt.class);
		assertThatThrownBy(() -> sample("Norte", 10, "06:00", "22:01", "20.00")).isInstanceOf(InvalidCourt.class);
	}

	@Test
	void rejectsARateBelowFive() {
		assertThatThrownBy(() -> sample("Norte", 10, "06:00", "22:00", "4.99")).isInstanceOf(InvalidCourt.class);
	}

	@Test
	void rejectsMoreThanTwoDecimalPlaces() {
		assertThatThrownBy(() -> sample("Norte", 10, "06:00", "22:00", "5.001")).isInstanceOf(InvalidCourt.class);
	}

	@Test
	void rejectsABlankSportType() {
		assertThatThrownBy(() -> court("Norte", "  ", 10, "06:00", "22:00", "5.00")).isInstanceOf(InvalidCourt.class);
	}

	private static Court sample(String name, int capacity, String opensAt, String closesAt, String hourlyRate) {
		return court(name, "FOOTBALL", capacity, opensAt, closesAt, hourlyRate);
	}

	private static Court court(String name, String sportTypeCode, int capacity, String opensAt, String closesAt,
			String hourlyRate) {
		return new Court(4L, name, sportTypeCode, capacity, LocalTime.parse(opensAt), LocalTime.parse(closesAt),
				new BigDecimal(hourlyRate), false);
	}

}
