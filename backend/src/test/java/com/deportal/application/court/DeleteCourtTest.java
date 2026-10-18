package com.deportal.application.court;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import com.deportal.domain.court.Court;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.court.UpcomingReservations;
import com.deportal.domain.court.failure.CourtHasUpcomingReservations;
import com.deportal.domain.court.failure.CourtNotFound;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeleteCourtTest {

	private final Clock clock = Clock.fixed(Instant.parse("2026-10-18T15:00:00Z"), ZoneOffset.UTC);

	@Test
	void rejectsACourtWithUpcomingReservations() {
		RecordingCourts courts = new RecordingCourts(true);

		assertThatThrownBy(() -> new DeleteCourt(courts, courts, clock).execute(4L))
			.isInstanceOf(CourtHasUpcomingReservations.class);
		assertThat(courts.deletedId).isNull();
	}

	@Test
	void softDeletesACourtWithoutUpcomingReservations() {
		RecordingCourts courts = new RecordingCourts(false);

		new DeleteCourt(courts, courts, clock).execute(4L);

		assertThat(courts.deletedId).isEqualTo(4L);
		assertThat(courts.checkedOn).isEqualTo(LocalDate.of(2026, 10, 18));
	}

	@Test
	void rejectsAMissingCourt() {
		RecordingCourts courts = new RecordingCourts(false);

		assertThatThrownBy(() -> new DeleteCourt(courts, courts, clock).execute(9L)).isInstanceOf(CourtNotFound.class);
	}

	private static final class RecordingCourts implements CourtRepository, UpcomingReservations {

		private final boolean upcoming;
		private Long deletedId;
		private LocalDate checkedOn;

		private RecordingCourts(boolean upcoming) {
			this.upcoming = upcoming;
		}

		@Override
		public boolean existsForCourt(Long courtId, LocalDate today) {
			checkedOn = today;
			return upcoming && courtId.equals(4L);
		}

		@Override
		public List<Court> findActive() {
			return List.of();
		}

		@Override
		public Optional<Court> findActiveById(Long id) {
			if (!id.equals(4L)) {
				return Optional.empty();
			}
			return Optional.of(new Court(4L, "Norte", "FOOTBALL", 10, LocalTime.of(6, 0), LocalTime.of(22, 0),
					new BigDecimal("20.00"), false));
		}

		@Override
		public boolean existsActiveName(String name, Long excludingId) {
			return false;
		}

		@Override
		public Court save(Court court) {
			return court;
		}

		@Override
		public void softDelete(Long id) {
			deletedId = id;
		}

	}

}
