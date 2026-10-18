package com.deportal.domain.reservation;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class CourtWindow {

	public static final int MIN_DURATION_HOURS = 1;
	public static final int MAX_DURATION_HOURS = 8;

	private CourtWindow() {
	}

	public static boolean durationIsValid(int hours) {
		return hours >= MIN_DURATION_HOURS && hours <= MAX_DURATION_HOURS;
	}

	public static boolean fits(LocalTime opensAt, LocalTime closesAt, LocalTime start, int hours) {
		if (!durationIsValid(hours) || start == null || opensAt == null || closesAt == null || start.isBefore(opensAt)) {
			return false;
		}
		int end = start.getHour() * 60 + start.getMinute() + hours * 60;
		int close = closesAt.getHour() * 60 + closesAt.getMinute();
		return end <= close;
	}

	public static List<LocalTime> starts(LocalTime opensAt, LocalTime closesAt, int hours) {
		List<LocalTime> times = new ArrayList<>();
		if (!durationIsValid(hours) || opensAt == null || closesAt == null) {
			return times;
		}
		LocalTime cursor = opensAt;
		while (fits(opensAt, closesAt, cursor, hours)) {
			times.add(cursor);
			cursor = cursor.plusHours(1);
		}
		return times;
	}

}
