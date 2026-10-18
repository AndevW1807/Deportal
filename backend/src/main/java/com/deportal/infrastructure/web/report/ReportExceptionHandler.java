package com.deportal.infrastructure.web.report;

import com.deportal.domain.report.failure.InvalidReportPeriod;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReportExceptionHandler {

	@ExceptionHandler(InvalidReportPeriod.class)
	ProblemDetail invalidPeriod() {
		return problem(HttpStatus.BAD_REQUEST, "Bad Request", InvalidReportPeriod.MESSAGE);
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatus(status);
		problem.setTitle(title);
		problem.setDetail(detail);
		return problem;
	}

}
