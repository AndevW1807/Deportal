package com.deportal.application.auth.failure;

public final class InvalidRegistration extends RuntimeException {

	public InvalidRegistration() {
		super("Invalid request");
	}

}
