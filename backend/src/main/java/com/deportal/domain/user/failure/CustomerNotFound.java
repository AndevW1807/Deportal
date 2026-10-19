package com.deportal.domain.user.failure;

public final class CustomerNotFound extends RuntimeException {

	public static final String MESSAGE = "Customer not found";

	public CustomerNotFound() {
		super(MESSAGE);
	}

}
