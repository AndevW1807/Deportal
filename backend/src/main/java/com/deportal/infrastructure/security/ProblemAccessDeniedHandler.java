package com.deportal.infrastructure.security;

import java.io.IOException;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

final class ProblemAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	ProblemAccessDeniedHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		ProblemDetail problem = ProblemDetail.forStatus(HttpServletResponse.SC_FORBIDDEN);
		problem.setTitle("Forbidden");
		String detail = request.getRequestURI() != null && request.getRequestURI().startsWith("/api/reservations")
				? "Customer access required"
				: "Admin access required";
		problem.setDetail(detail);
		objectMapper.writeValue(response.getOutputStream(), problem);
	}

}
