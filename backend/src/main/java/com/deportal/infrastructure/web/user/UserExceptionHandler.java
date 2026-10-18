package com.deportal.infrastructure.web.user;

import com.deportal.domain.user.CustomerNotFound;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler {

	@ExceptionHandler(CustomerNotFound.class)
	ProblemDetail customerNotFound() {
		return problem(HttpStatus.NOT_FOUND, "Not Found", CustomerNotFound.MESSAGE);
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatus(status);
		problem.setTitle(title);
		problem.setDetail(detail);
		return problem;
	}

}
