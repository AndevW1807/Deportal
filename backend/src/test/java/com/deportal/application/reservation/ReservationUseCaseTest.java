package com.deportal.application.reservation;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.deportal.domain.court.Court;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.reservation.Reservation;
import com.deportal.domain.reservation.ReservationRepository;
import com.deportal.domain.reservation.ReservationStatus;
import com.deportal.domain.reservation.failure.InvalidReservation;
import com.deportal.domain.reservation.failure.ReservationConflict;
import com.deportal.domain.reservation.failure.ReservationNotFound;
import com.deportal.domain.user.CustomerNotFound;
import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserAccount;
import com.deportal.domain.user.UserRepository;
import com.deportal.domain.user.UserRole;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationUseCaseTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 10, 18);
	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-18T20:00:00Z"), ZoneOffset.UTC);
	private static final Court COURT = new Court(4L, "Norte", "FOOTBALL", 10, LocalTime.of(6, 0), LocalTime.of(22, 0),
			new BigDecimal("20.00"), false);

	@Test
	void confirmsAFreeSlotAndStoresTheMemberDiscount() {
		Memory memory = new Memory();

		Reservation saved = create(memory, MembershipType.MEMBER).execute(7L, 4L, TODAY, LocalTime.of(16, 0), 2)
			.reservation();

		assertThat(saved.status()).isEqualTo(ReservationStatus.CONFIRMED);
		assertThat(saved.discountRate()).isEqualByComparingTo("0.10");
		assertThat(saved.total()).isEqualByComparingTo("36.00");
	}

	@Test
	void waitsASameDayNonMemberWhenTheSlotOverlaps() {
		Memory memory = new Memory();
		memory.rows.add(booked(1L, ReservationStatus.CONFIRMED, TODAY, LocalTime.of(10, 0), 2));

		Reservation saved = create(memory, MembershipType.NON_MEMBER).execute(7L, 4L, TODAY, LocalTime.of(16, 0), 2)
			.reservation();

		assertThat(saved.status()).isEqualTo(ReservationStatus.CONFIRMED);
		saved = create(memory, MembershipType.NON_MEMBER).execute(7L, 4L, TODAY, LocalTime.of(16, 0), 1).reservation();
		assertThat(saved.status()).isEqualTo(ReservationStatus.WAITING);
	}

	@Test
	void waitsASameDayMemberAndRejectsAFutureDayWhenTheSlotIsTaken() {
		Memory memory = new Memory();
		memory.rows.add(booked(1L, ReservationStatus.CONFIRMED, TODAY, LocalTime.of(16, 0), 2));
		memory.rows.add(booked(2L, ReservationStatus.CONFIRMED, TODAY.plusDays(1), LocalTime.of(16, 0), 2));

		Reservation saved = create(memory, MembershipType.MEMBER).execute(7L, 4L, TODAY, LocalTime.of(16, 0), 1)
			.reservation();
		assertThat(saved.status()).isEqualTo(ReservationStatus.WAITING);
		assertThatThrownBy(
				() -> create(memory, MembershipType.MEMBER).execute(7L, 4L, TODAY.plusDays(1), LocalTime.of(16, 0), 1))
			.isInstanceOf(ReservationConflict.class);
	}

	@Test
	void rejectsASlotThatOnlyBreaksTheCleaningHour() {
		Memory memory = new Memory();
		memory.rows.add(booked(1L, ReservationStatus.CONFIRMED, TODAY.plusDays(1), LocalTime.of(10, 0), 2));

		assertThatThrownBy(
				() -> create(memory, MembershipType.NON_MEMBER).execute(7L, 4L, TODAY.plusDays(1), LocalTime.of(12, 0), 1))
			.isInstanceOf(ReservationConflict.class);
	}

	@Test
	void rejectsAPastStartAndANonCustomer() {
		Memory memory = new Memory();

		assertThatThrownBy(() -> create(memory, MembershipType.MEMBER).execute(7L, 4L, TODAY, LocalTime.of(15, 0), 1))
			.isInstanceOf(InvalidReservation.class);
		assertThatThrownBy(() -> create(memory, MembershipType.MEMBER, UserRole.ADMIN).execute(7L, 4L, TODAY,
				LocalTime.of(16, 0), 1)).isInstanceOf(CustomerNotFound.class);
	}

	@Test
	void marksOccupiedAndCleaningSlots() {
		Memory memory = new Memory();
		memory.rows.add(booked(1L, ReservationStatus.CONFIRMED, TODAY, LocalTime.of(16, 0), 2));

		List<AvailableStart> starts = new ListAvailability(new Courts(), memory, CLOCK).execute(4L, TODAY, 1);

		assertThat(start(starts, 17)).isEqualTo(AvailableStart.SlotState.OCCUPIED);
		assertThat(start(starts, 18)).isEqualTo(AvailableStart.SlotState.BLOCKED);
		assertThat(start(starts, 19)).isEqualTo(AvailableStart.SlotState.FREE);
		assertThat(starts).noneMatch(slot -> slot.startsAt().equals(LocalTime.of(15, 0)));
	}

	@Test
	void promotesTheOldestWaitingReservationThatFits() {
		Memory memory = new Memory();
		memory.rows.add(booked(1L, ReservationStatus.CONFIRMED, TODAY, LocalTime.of(16, 0), 2));
		memory.rows.add(booked(2L, ReservationStatus.WAITING, TODAY, LocalTime.of(16, 0), 1));
		memory.rows.add(booked(3L, ReservationStatus.WAITING, TODAY, LocalTime.of(17, 0), 1));

		new CancelReservation(memory, new Courts(), accounts()).execute(7L, 1L);

		assertThat(memory.rows.get(0).status()).isEqualTo(ReservationStatus.CANCELLED);
		assertThat(memory.rows.get(1).status()).isEqualTo(ReservationStatus.CONFIRMED);
		assertThat(memory.rows.get(2).status()).isEqualTo(ReservationStatus.WAITING);
	}

	@Test
	void doesNotPromoteWhenAWaitingReservationIsCancelled() {
		Memory memory = new Memory();
		memory.rows.add(booked(2L, ReservationStatus.WAITING, TODAY, LocalTime.of(16, 0), 1));

		new CancelReservation(memory, new Courts(), accounts()).execute(7L, 2L);

		assertThat(memory.rows.get(0).status()).isEqualTo(ReservationStatus.CANCELLED);
	}

	@Test
	void hidesAnotherCustomersReservation() {
		Memory memory = new Memory();
		memory.rows.add(booked(1L, ReservationStatus.CONFIRMED, TODAY, LocalTime.of(16, 0), 1));

		assertThatThrownBy(() -> new CancelReservation(memory, new Courts(), accounts()).execute(8L, 1L))
			.isInstanceOf(ReservationNotFound.class);
	}

	@Test
	void promotesALaterMemberBeforeAnOlderGuest() {
		Memory memory = new Memory();
		memory.rows.add(booked(1L, 7L, ReservationStatus.CONFIRMED, TODAY, LocalTime.of(16, 0), 1));
		memory.rows.add(booked(2L, 8L, ReservationStatus.WAITING, TODAY, LocalTime.of(16, 0), 1));
		memory.rows.add(booked(3L, 9L, ReservationStatus.WAITING, TODAY, LocalTime.of(16, 0), 1));

		new CancelReservation(memory, new Courts(), accounts(9L)).execute(7L, 1L);

		assertThat(memory.rows.get(1).status()).isEqualTo(ReservationStatus.WAITING);
		assertThat(memory.rows.get(2).status()).isEqualTo(ReservationStatus.CONFIRMED);
	}

	private static CreateReservation create(Memory memory, MembershipType type) {
		return create(memory, type, UserRole.CUSTOMER);
	}

	private static CreateReservation create(Memory memory, MembershipType type, UserRole role) {
		return new CreateReservation(new Courts(), memory.users(type, role), memory, CLOCK);
	}

	private static AvailableStart.SlotState start(List<AvailableStart> starts, int hour) {
		return starts.stream()
			.filter(slot -> slot.startsAt().equals(LocalTime.of(hour, 0)))
			.findFirst()
			.orElseThrow()
			.state();
	}

	private static Reservation booked(Long id, ReservationStatus status, LocalDate date, LocalTime start, int hours) {
		return booked(id, 7L, status, date, start, hours);
	}

	private static Reservation booked(Long id, Long customerId, ReservationStatus status, LocalDate date, LocalTime start,
			int hours) {
		return new Reservation(id, customerId, 4L, date, start, hours, status, new BigDecimal("20.00"),
				new BigDecimal("0.00"), new BigDecimal("20.00"));
	}

	private static UserRepository accounts(Long... memberIds) {
		Set<Long> members = Set.of(memberIds);
		return new UserRepository() {
			@Override
			public Optional<UserAccount> findByEmail(String email) {
				return Optional.empty();
			}

			@Override
			public Optional<UserAccount> findActiveById(Long id) {
				MembershipType type = members.contains(id) ? MembershipType.MEMBER : MembershipType.NON_MEMBER;
				return Optional.of(new UserAccount(id, "Cliente", "cliente@deportal.test", "hash", UserRole.CUSTOMER,
						type, false));
			}

			@Override
			public List<UserAccount> findActiveCustomers() {
				return List.of();
			}

			@Override
			public UserAccount save(UserAccount saved) {
				return saved;
			}
		};
	}

	private static final class Courts implements CourtRepository {

		@Override
		public List<Court> findActive() {
			return List.of(COURT);
		}

		@Override
		public Optional<Court> findActiveById(Long id) {
			return id.equals(4L) ? Optional.of(COURT) : Optional.empty();
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

	}

	private static final class Memory implements ReservationRepository {

		private final List<Reservation> rows = new ArrayList<>();
		private long sequence = 10;

		private UserRepository users(MembershipType type, UserRole role) {
			UserAccount account = new UserAccount(7L, "Juan", "juan@deportal.test", "hash", role, type, false);
			return new UserRepository() {
				@Override
				public Optional<UserAccount> findByEmail(String email) {
					return Optional.empty();
				}

				@Override
				public Optional<UserAccount> findActiveById(Long id) {
					return id.equals(7L) ? Optional.of(account) : Optional.empty();
				}

				@Override
				public List<UserAccount> findActiveCustomers() {
					return List.of(account);
				}

				@Override
				public UserAccount save(UserAccount saved) {
					return saved;
				}
			};
		}

		@Override
		public Reservation save(Reservation reservation) {
			return saveAll(List.of(reservation)).get(0);
		}

		@Override
		public List<Reservation> saveAll(List<Reservation> reservations) {
			List<Reservation> saved = new ArrayList<>();
			for (Reservation reservation : reservations) {
				Reservation stored = reservation.id() == null ? withId(reservation, sequence++) : reservation;
				rows.removeIf(row -> row.id().equals(stored.id()));
				rows.add(stored);
				saved.add(stored);
			}
			rows.sort((left, right) -> left.id().compareTo(right.id()));
			return saved;
		}

		@Override
		public List<Reservation> findConfirmed(Long courtId, LocalDate date) {
			return rows.stream()
				.filter(row -> row.courtId().equals(courtId) && row.date().equals(date))
				.filter(row -> row.status() == ReservationStatus.CONFIRMED)
				.toList();
		}

		@Override
		public List<Reservation> findWaiting(Long courtId, LocalDate date) {
			return rows.stream()
				.filter(row -> row.courtId().equals(courtId) && row.date().equals(date))
				.filter(row -> row.status() == ReservationStatus.WAITING)
				.toList();
		}

		@Override
		public List<Reservation> findActiveByCustomer(Long customerId) {
			return rows.stream().filter(row -> row.customerId().equals(customerId)).toList();
		}

		@Override
		public Optional<Reservation> findActiveById(Long id) {
			return rows.stream()
				.filter(row -> row.id().equals(id) && row.status() != ReservationStatus.CANCELLED)
				.findFirst();
		}

		private static Reservation withId(Reservation reservation, long id) {
			return new Reservation(id, reservation.customerId(), reservation.courtId(), reservation.date(),
					reservation.startsAt(), reservation.durationHours(), reservation.status(), reservation.hourlyRate(),
					reservation.discountRate(), reservation.total());
		}

	}

}
