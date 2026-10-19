package com.deportal.domain.user.failure;

public final class EmailAlreadyUsed extends RuntimeException {

	public static final String MESSAGE = "Email already registered";

	public EmailAlreadyUsed() {
		super(MESSAGE);
	}

}
