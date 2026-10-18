package com.deportal.domain.reservation.failure;

public final class ReservationConflict extends RuntimeException {

	public static final String MESSAGE = "The court is not available at that time";

	public ReservationConflict() {
		super(MESSAGE);
	}

}
