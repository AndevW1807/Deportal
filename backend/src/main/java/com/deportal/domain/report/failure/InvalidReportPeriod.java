package com.deportal.domain.report.failure;

public final class InvalidReportPeriod extends RuntimeException {

	public static final String MESSAGE = "Invalid request";

	public InvalidReportPeriod() {
		super(MESSAGE);
	}

}
