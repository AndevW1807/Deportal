package com.deportal.application.court;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.deportal.domain.court.Court;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.court.SportType;
import com.deportal.domain.court.SportTypeCatalog;
import com.deportal.domain.court.failure.CourtNameAlreadyUsed;
import com.deportal.domain.court.failure.InvalidCourt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateCourtTest {

	@Test
	void rejectsANameUsedByAnotherCourt() {
		MemoryCourts courts = new MemoryCourts(football("Norte"), football("Sur"));

		assertThatThrownBy(() -> update(courts).execute(1L, "Sur", "FOOTBALL", 10, LocalTime.of(6, 0),
				LocalTime.of(22, 0), new BigDecimal("20.00")))
			.isInstanceOf(CourtNameAlreadyUsed.class);
	}

	@Test
	void keepsTheSameNameOnTheSameCourt() {
		MemoryCourts courts = new MemoryCourts(football("Norte"));

		CourtDetails updated = update(courts).execute(1L, "  Norte  ", "FOOTBALL", 12, LocalTime.of(7, 0),
				LocalTime.of(21, 0), new BigDecimal("15"));

		assertThat(updated.court().name()).isEqualTo("Norte");
		assertThat(updated.court().capacity()).isEqualTo(12);
		assertThat(updated.sportType().name()).isEqualTo("Fútbol");
		assertThat(courts.saved.name()).isEqualTo("Norte");
	}

	@Test
	void rejectsAnUnknownSport() {
		MemoryCourts courts = new MemoryCourts(football("Norte"));

		assertThatThrownBy(() -> update(courts).execute(1L, "Norte", "MISSING", 10, LocalTime.of(6, 0),
				LocalTime.of(22, 0), new BigDecimal("20.00")))
			.isInstanceOf(InvalidCourt.class);
	}

	private static UpdateCourt update(MemoryCourts courts) {
		return new UpdateCourt(courts, new MemorySports());
	}

	private static Court football(String name) {
		long id = "Norte".equals(name) ? 1L : 2L;
		return new Court(id, name, "FOOTBALL", 10, LocalTime.of(6, 0), LocalTime.of(22, 0), new BigDecimal("20.00"),
				false);
	}

	private static final class MemoryCourts implements CourtRepository {

		private final List<Court> active;
		private Court saved;

		private MemoryCourts(Court... courts) {
			this.active = new ArrayList<>(List.of(courts));
		}

		@Override
		public List<Court> findActive() {
			return List.copyOf(active);
		}

		@Override
		public Optional<Court> findActiveById(Long id) {
			return active.stream().filter(court -> court.id().equals(id)).findFirst();
		}

		@Override
		public boolean existsActiveName(String name, Long excludingId) {
			return active.stream()
				.anyMatch(court -> court.name().equalsIgnoreCase(name) && !court.id().equals(excludingId));
		}

		@Override
		public Court save(Court court) {
			saved = court;
			return court;
		}

		@Override
		public void softDelete(Long id) {
		}

	}

	private static final class MemorySports implements SportTypeCatalog {

		@Override
		public List<SportType> findActive() {
			return List.of(new SportType("FOOTBALL", "Fútbol"));
		}

		@Override
		public boolean exists(String code) {
			return "FOOTBALL".equals(code);
		}

	}

}
