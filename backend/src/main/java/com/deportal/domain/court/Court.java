package com.deportal.domain.court;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;

import com.deportal.domain.court.failure.InvalidCourt;

public final class Court {

	public static final int MIN_CAPACITY = 1;
	public static final int MAX_CAPACITY = 50;
	public static final int MAX_NAME_LENGTH = 120;
	public static final int MAX_SPORT_CODE_LENGTH = 32;
	public static final LocalTime GLOBAL_OPENS = LocalTime.of(6, 0);
	public static final LocalTime GLOBAL_CLOSES = LocalTime.of(22, 0);
	public static final BigDecimal MIN_HOURLY_RATE = new BigDecimal("5.00");

	private final Long id;
	private final String name;
	private final String sportTypeCode;
	private final int capacity;
	private final LocalTime opensAt;
	private final LocalTime closesAt;
	private final BigDecimal hourlyRate;
	private final boolean deleted;

	public Court(Long id, String name, String sportTypeCode, int capacity, LocalTime opensAt, LocalTime closesAt,
			BigDecimal hourlyRate, boolean deleted) {
		this.id = id;
		this.name = normalizeName(name);
		this.sportTypeCode = normalizeSportCode(sportTypeCode);
		this.capacity = capacity;
		this.opensAt = opensAt;
		this.closesAt = closesAt;
		this.hourlyRate = normalizeRate(hourlyRate);
		this.deleted = deleted;
		if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY || !scheduleIsValid(opensAt, closesAt)) {
			throw new InvalidCourt();
		}
	}

	public Court revise(String name, String sportTypeCode, int capacity, LocalTime opensAt, LocalTime closesAt,
			BigDecimal hourlyRate) {
		return new Court(id, name, sportTypeCode, capacity, opensAt, closesAt, hourlyRate, false);
	}

	public Long id() {
		return id;
	}

	public String name() {
		return name;
	}

	public String sportTypeCode() {
		return sportTypeCode;
	}

	public int capacity() {
		return capacity;
	}

	public LocalTime opensAt() {
		return opensAt;
	}

	public LocalTime closesAt() {
		return closesAt;
	}

	public BigDecimal hourlyRate() {
		return hourlyRate;
	}

	public boolean deleted() {
		return deleted;
	}

	private static String normalizeName(String name) {
		if (name == null) {
			throw new InvalidCourt();
		}
		String trimmed = name.trim();
		if (trimmed.isEmpty() || trimmed.length() > MAX_NAME_LENGTH) {
			throw new InvalidCourt();
		}
		return trimmed;
	}

	private static String normalizeSportCode(String sportTypeCode) {
		if (sportTypeCode == null) {
			throw new InvalidCourt();
		}
		String trimmed = sportTypeCode.trim();
		if (trimmed.isEmpty() || trimmed.length() > MAX_SPORT_CODE_LENGTH) {
			throw new InvalidCourt();
		}
		return trimmed;
	}

	private static BigDecimal normalizeRate(BigDecimal hourlyRate) {
		if (hourlyRate == null) {
			throw new InvalidCourt();
		}
		try {
			BigDecimal scaled = hourlyRate.setScale(2, RoundingMode.UNNECESSARY);
			if (scaled.compareTo(MIN_HOURLY_RATE) < 0) {
				throw new InvalidCourt();
			}
			return scaled;
		}
		catch (ArithmeticException exception) {
			throw new InvalidCourt();
		}
	}

	private static boolean scheduleIsValid(LocalTime opensAt, LocalTime closesAt) {
		if (opensAt == null || closesAt == null) {
			return false;
		}
		return !opensAt.isBefore(GLOBAL_OPENS) && !closesAt.isAfter(GLOBAL_CLOSES) && opensAt.isBefore(closesAt);
	}

}
