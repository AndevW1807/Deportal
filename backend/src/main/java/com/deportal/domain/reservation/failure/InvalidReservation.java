package com.deportal.domain.reservation.failure;

public final class InvalidReservation extends RuntimeException {

	public static final String MESSAGE = "Invalid request";

	public InvalidReservation() {
		super(MESSAGE);
	}

}
