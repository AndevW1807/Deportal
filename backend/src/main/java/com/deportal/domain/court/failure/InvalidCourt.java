package com.deportal.domain.court.failure;

public final class InvalidCourt extends RuntimeException {

	public static final String MESSAGE = "Invalid request";

	public InvalidCourt() {
		super(MESSAGE);
	}

}
