package com.deportal.infrastructure.web.reservation;

import java.time.format.DateTimeFormatter;

import com.deportal.application.reservation.AvailableStart;

public record AvailableStartResponse(String startsAt, String state) {

	private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH:mm");

	static AvailableStartResponse from(AvailableStart start) {
		return new AvailableStartResponse(HOUR.format(start.startsAt()), start.state().name());
	}

}
