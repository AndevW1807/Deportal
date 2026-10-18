package com.deportal.application.auth.failure;

public final class PasswordTooLong extends RuntimeException {

	public static final int MAX_BYTES = 72;

	public PasswordTooLong() {
		super("Invalid request");
	}

}
