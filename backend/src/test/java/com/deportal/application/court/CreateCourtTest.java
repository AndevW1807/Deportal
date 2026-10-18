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

class CreateCourtTest {

	@Test
	void createsACourtWithANewId() {
		MemoryCourts courts = new MemoryCourts();

		CourtDetails created = create(courts).execute("  Cancha Norte  ", "FOOTBALL", 10, LocalTime.of(6, 0),
				LocalTime.of(22, 0), new BigDecimal("20.00"));

		assertThat(created.court().id()).isEqualTo(8L);
		assertThat(created.court().name()).isEqualTo("Cancha Norte");
		assertThat(created.sportType().name()).isEqualTo("Fútbol");
		assertThat(courts.saved.id()).isNull();
	}

	@Test
	void rejectsANameAlreadyRegistered() {
		MemoryCourts courts = new MemoryCourts(football("Norte"));

		assertThatThrownBy(() -> create(courts).execute("norte", "FOOTBALL", 10, LocalTime.of(6, 0),
				LocalTime.of(22, 0), new BigDecimal("20.00")))
			.isInstanceOf(CourtNameAlreadyUsed.class);
	}

	@Test
	void rejectsAnUnknownSport() {
		MemoryCourts courts = new MemoryCourts();

		assertThatThrownBy(() -> create(courts).execute("Norte", "MISSING", 10, LocalTime.of(6, 0),
				LocalTime.of(22, 0), new BigDecimal("20.00")))
			.isInstanceOf(InvalidCourt.class);
	}

	private static CreateCourt create(MemoryCourts courts) {
		return new CreateCourt(courts, new MemorySports());
	}

	private static Court football(String name) {
		return new Court(1L, name, "FOOTBALL", 10, LocalTime.of(6, 0), LocalTime.of(22, 0), new BigDecimal("20.00"),
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
			return new Court(8L, court.name(), court.sportTypeCode(), court.capacity(), court.opensAt(),
					court.closesAt(), court.hourlyRate(), false);
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
