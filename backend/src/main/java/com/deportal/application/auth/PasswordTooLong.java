package com.deportal.application.auth;

public final class PasswordTooLong extends RuntimeException {

	public PasswordTooLong() {
		super("Invalid request");
	}

}
