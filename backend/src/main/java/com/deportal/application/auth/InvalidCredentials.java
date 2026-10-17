package com.deportal.application.auth;

public final class InvalidCredentials extends RuntimeException {

	public static final String MESSAGE = "Invalid credentials";

	public InvalidCredentials() {
		super(MESSAGE);
	}

}
