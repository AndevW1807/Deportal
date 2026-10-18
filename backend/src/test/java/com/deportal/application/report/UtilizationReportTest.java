package com.deportal.application.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.deportal.domain.court.Court;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.court.CourtReservationUse;
import com.deportal.domain.court.ReservationUsage;
import com.deportal.domain.court.SportType;
import com.deportal.domain.court.SportTypeCatalog;
import com.deportal.domain.report.failure.InvalidReportPeriod;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UtilizationReportTest {

	private static final LocalDate FROM = LocalDate.of(2026, 10, 1);
	private static final LocalDate TO = LocalDate.of(2026, 10, 2);

	@Test
	void countsOpenHoursAndLeavesMissingUsageAtZero() {
		UtilizationReportView report = report(List.of(), " ").execute(FROM, TO, " ");

		assertThat(report.reservations()).isZero();
		assertThat(report.reservedHours()).isEqualByComparingTo("0.00");
		assertThat(report.income()).isEqualByComparingTo("0.00");
		assertThat(report.occupancyRate()).isEqualByComparingTo("0.00");
		assertThat(report.courts()).extracting(CourtUtilization::name).containsExactly("Norte", "Sur");
		assertThat(report.courts().get(0).availableHours()).isEqualByComparingTo("32.00");
		assertThat(report.courts().get(0).occupancyRate()).isEqualByComparingTo("0.00");
	}

	@Test
	void appliesReservationUsageAndTheSportFilter() {
		UtilizationReportView report = report(
				List.of(new CourtReservationUse(1L, 2, new BigDecimal("8.00"), new BigDecimal("160.00"))), "FOOTBALL")
			.execute(FROM, FROM, "FOOTBALL");

		assertThat(report.courts()).hasSize(1);
		assertThat(report.reservations()).isEqualTo(2);
		assertThat(report.reservedHours()).isEqualByComparingTo("8.00");
		assertThat(report.income()).isEqualByComparingTo("160.00");
		assertThat(report.occupancyRate()).isEqualByComparingTo("50.00");
		assertThat(report.courts().get(0).sportName()).isEqualTo("Fútbol");
		assertThat(report.courts().get(0).availableHours()).isEqualByComparingTo("16.00");
	}

	@Test
	void rejectsAnEndDateBeforeTheStart() {
		assertThatThrownBy(() -> report(List.of(), null).execute(TO, FROM, null)).isInstanceOf(InvalidReportPeriod.class);
	}

	private static UtilizationReport report(List<CourtReservationUse> usage, String sport) {
		return new UtilizationReport(new Courts(), new Sports(), (from, to) -> {
			assertThat(from).isEqualTo(FROM);
			assertThat(to).isAfterOrEqualTo(from);
			assertThat(sport).isNotNull();
			return usage;
		});
	}

	private static final class Courts implements CourtRepository {

		@Override
		public List<Court> findActive() {
			return List.of(
					court(2L, "Sur", "TENNIS", LocalTime.of(8, 0), LocalTime.of(20, 0)),
					court(1L, "Norte", "FOOTBALL", LocalTime.of(6, 0), LocalTime.of(22, 0)));
		}

		@Override
		public Optional<Court> findActiveById(Long id) {
			return Optional.empty();
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
		}

		private static Court court(Long id, String name, String sport, LocalTime opens, LocalTime closes) {
			return new Court(id, name, sport, 10, opens, closes, new BigDecimal("20.00"), false);
		}

	}

	private static final class Sports implements SportTypeCatalog {

		@Override
		public List<SportType> findActive() {
			return List.of(new SportType("FOOTBALL", "Fútbol"), new SportType("TENNIS", "Tenis"));
		}

		@Override
		public boolean exists(String code) {
			return true;
		}

	}

}
