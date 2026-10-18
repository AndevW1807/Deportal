package com.deportal.domain.reservation;

import java.time.LocalTime;

public final class ScheduleConflict {

	public static final int CLEANING_MINUTES = 60;

	private ScheduleConflict() {
	}

	public static boolean overlaps(LocalTime existingStart, int existingHours, LocalTime requestedStart,
			int requestedHours) {
		int existingFrom = minutes(existingStart);
		int existingTo = existingFrom + existingHours * 60;
		int requestedFrom = minutes(requestedStart);
		int requestedTo = requestedFrom + requestedHours * 60;
		return requestedFrom < existingTo && requestedTo > existingFrom;
	}

	public static boolean conflicts(LocalTime existingStart, int existingHours, LocalTime requestedStart,
			int requestedHours) {
		int existingFrom = minutes(existingStart) - CLEANING_MINUTES;
		int existingTo = minutes(existingStart) + existingHours * 60 + CLEANING_MINUTES;
		int requestedFrom = minutes(requestedStart);
		int requestedTo = requestedFrom + requestedHours * 60;
		return requestedFrom < existingTo && requestedTo > existingFrom;
	}

	private static int minutes(LocalTime time) {
		return time.getHour() * 60 + time.getMinute();
	}

}
