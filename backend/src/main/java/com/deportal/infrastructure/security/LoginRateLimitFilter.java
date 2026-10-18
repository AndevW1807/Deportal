package com.deportal.infrastructure.security;

import java.io.IOException;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;

public class LoginRateLimitFilter extends OncePerRequestFilter {

	private final LoginRateLimiter limiter;
	private final ObjectMapper objectMapper;

	public LoginRateLimitFilter(LoginRateLimiter limiter, ObjectMapper objectMapper) {
		this.limiter = limiter;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (!isLimited(request)) {
			filterChain.doFilter(request, response);
			return;
		}
		if (!limiter.allow(request.getRemoteAddr())) {
			response.setStatus(429);
			response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
			ProblemDetail problem = ProblemDetail.forStatus(429);
			problem.setTitle("Too Many Requests");
			problem.setDetail(detail(request));
			objectMapper.writeValue(response.getOutputStream(), problem);
			return;
		}
		filterChain.doFilter(request, response);
	}

	private static boolean isLimited(HttpServletRequest request) {
		if (!HttpMethod.POST.matches(request.getMethod())) {
			return false;
		}
		String path = request.getServletPath();
		return "/api/auth/login".equals(path) || "/api/auth/register".equals(path);
	}

	private static String detail(HttpServletRequest request) {
		if ("/api/auth/register".equals(request.getServletPath())) {
			return "Too many registration attempts";
		}
		return "Too many login attempts";
	}

}
