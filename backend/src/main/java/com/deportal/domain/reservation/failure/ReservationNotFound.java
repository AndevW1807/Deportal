package com.deportal.domain.reservation.failure;

public final class ReservationNotFound extends RuntimeException {

	public static final String MESSAGE = "Reservation not found";

	public ReservationNotFound() {
		super(MESSAGE);
	}

}
