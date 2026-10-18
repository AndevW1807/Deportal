package com.deportal.infrastructure.web.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(@NotNull Long courtId, @NotNull LocalDate date, @NotNull LocalTime startsAt,
		@NotNull Integer durationHours) {
}
