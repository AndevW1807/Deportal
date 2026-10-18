package com.deportal.domain.court.failure;

public final class CourtNotFound extends RuntimeException {

	public static final String MESSAGE = "Court not found";

	public CourtNotFound() {
		super(MESSAGE);
	}

}
