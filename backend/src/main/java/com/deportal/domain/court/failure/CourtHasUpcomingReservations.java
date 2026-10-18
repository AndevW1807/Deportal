package com.deportal.domain.court.failure;

public final class CourtHasUpcomingReservations extends RuntimeException {

	public static final String MESSAGE = "Court has upcoming reservations";

	public CourtHasUpcomingReservations() {
		super(MESSAGE);
	}

}
