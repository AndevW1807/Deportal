package com.deportal.application.reservation;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import com.deportal.domain.reservation.Reservation;

public final class ReservationTime {

	public static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

	private ReservationTime() {
	}

	public static LocalDate today(Clock clock) {
		return LocalDate.now(clock.withZone(BOGOTA));
	}

	public static LocalTime now(Clock clock) {
		return LocalTime.now(clock.withZone(BOGOTA));
	}

	public static Duration untilStart(Reservation reservation, Clock clock) {
		LocalDateTime current = LocalDateTime.now(clock.withZone(BOGOTA));
		return Duration.between(current, LocalDateTime.of(reservation.date(), reservation.startsAt()));
	}

}
