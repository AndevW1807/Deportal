package com.deportal.infrastructure.web;

import com.deportal.application.auth.failure.InvalidCredentials;
import com.deportal.application.auth.failure.InvalidRegistration;
import com.deportal.application.auth.failure.PasswordTooLong;
import com.deportal.domain.user.EmailAlreadyUsed;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

	@ExceptionHandler(InvalidCredentials.class)
	ProblemDetail invalidCredentials() {
		return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", InvalidCredentials.MESSAGE);
	}

	@ExceptionHandler(EmailAlreadyUsed.class)
	ProblemDetail emailAlreadyUsed() {
		return problem(HttpStatus.CONFLICT, "Conflict", EmailAlreadyUsed.MESSAGE);
	}

	@ExceptionHandler({ MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
			PasswordTooLong.class, InvalidRegistration.class })
	ProblemDetail invalidRequest() {
		return problem(HttpStatus.BAD_REQUEST, "Bad Request", "Invalid request");
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatus(status);
		problem.setTitle(title);
		problem.setDetail(detail);
		return problem;
	}

}
