package com.deportal.domain.court.failure;

public final class CourtNameAlreadyUsed extends RuntimeException {

	public static final String MESSAGE = "Court name already registered";

	public CourtNameAlreadyUsed() {
		super(MESSAGE);
	}

}
