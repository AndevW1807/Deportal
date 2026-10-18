package com.deportal.infrastructure.web.court;

import com.deportal.domain.court.failure.CourtHasUpcomingReservations;
import com.deportal.domain.court.failure.CourtNameAlreadyUsed;
import com.deportal.domain.court.failure.CourtNotFound;
import com.deportal.domain.court.failure.InvalidCourt;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CourtExceptionHandler {

	@ExceptionHandler(InvalidCourt.class)
	ProblemDetail invalidCourt() {
		return problem(HttpStatus.BAD_REQUEST, "Bad Request", InvalidCourt.MESSAGE);
	}

	@ExceptionHandler(CourtNotFound.class)
	ProblemDetail courtNotFound() {
		return problem(HttpStatus.NOT_FOUND, "Not Found", CourtNotFound.MESSAGE);
	}

	@ExceptionHandler(CourtNameAlreadyUsed.class)
	ProblemDetail courtNameAlreadyUsed() {
		return problem(HttpStatus.CONFLICT, "Conflict", CourtNameAlreadyUsed.MESSAGE);
	}

	@ExceptionHandler(CourtHasUpcomingReservations.class)
	ProblemDetail courtHasUpcomingReservations() {
		return problem(HttpStatus.CONFLICT, "Conflict", CourtHasUpcomingReservations.MESSAGE);
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatus(status);
		problem.setTitle(title);
		problem.setDetail(detail);
		return problem;
	}

}
