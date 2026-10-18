package com.deportal.infrastructure.web.reservation;

import com.deportal.domain.reservation.failure.InvalidReservation;
import com.deportal.domain.reservation.failure.ReservationConflict;
import com.deportal.domain.reservation.failure.ReservationNotFound;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReservationExceptionHandler {

	@ExceptionHandler(InvalidReservation.class)
	ProblemDetail invalidReservation() {
		return problem(HttpStatus.BAD_REQUEST, "Bad Request", InvalidReservation.MESSAGE);
	}

	@ExceptionHandler(ReservationConflict.class)
	ProblemDetail reservationConflict() {
		return problem(HttpStatus.CONFLICT, "Conflict", ReservationConflict.MESSAGE);
	}

	@ExceptionHandler(ReservationNotFound.class)
	ProblemDetail reservationNotFound() {
		return problem(HttpStatus.NOT_FOUND, "Not Found", ReservationNotFound.MESSAGE);
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatus(status);
		problem.setTitle(title);
		problem.setDetail(detail);
		return problem;
	}

}
