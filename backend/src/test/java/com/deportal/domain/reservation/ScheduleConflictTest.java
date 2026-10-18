package com.deportal.domain.reservation;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleConflictTest {

	@Test
	void rejectsAnOverlapAndAMissingCleaningHour() {
		LocalTime existing = LocalTime.of(10, 0);

		assertThat(ScheduleConflict.overlaps(existing, 2, LocalTime.of(11, 0), 2)).isTrue();
		assertThat(ScheduleConflict.conflicts(existing, 2, LocalTime.of(12, 0), 1)).isTrue();
		assertThat(ScheduleConflict.overlaps(existing, 2, LocalTime.of(12, 0), 1)).isFalse();
		assertThat(ScheduleConflict.conflicts(existing, 2, LocalTime.of(13, 0), 1)).isFalse();
	}

}
