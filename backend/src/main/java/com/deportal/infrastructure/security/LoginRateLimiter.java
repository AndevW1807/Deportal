package com.deportal.infrastructure.security;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

public final class LoginRateLimiter {

	static final int LIMIT = 10;

	private static final Duration WINDOW = Duration.ofMinutes(1);

	private final ConcurrentHashMap<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

	public boolean allow(String clientKey) {
		String key = clientKey == null || clientKey.isBlank() ? "unknown" : clientKey;
		Instant now = Instant.now();
		Deque<Instant> window = attempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
		synchronized (window) {
			Instant cutoff = now.minus(WINDOW);
			while (!window.isEmpty() && window.peekFirst().isBefore(cutoff)) {
				window.removeFirst();
			}
			if (window.size() >= LIMIT) {
				return false;
			}
			window.addLast(now);
			return true;
		}
	}

}
