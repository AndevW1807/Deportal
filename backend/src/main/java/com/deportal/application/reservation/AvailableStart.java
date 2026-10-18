package com.deportal.application.reservation;

import java.time.LocalTime;
import java.util.List;

import com.deportal.domain.reservation.Reservation;
import com.deportal.domain.reservation.ScheduleConflict;

public record AvailableStart(LocalTime startsAt, SlotState state) {

	public enum SlotState {
		FREE,
		OCCUPIED,
		BLOCKED
	}

	public static AvailableStart classify(LocalTime startsAt, int durationHours, List<Reservation> confirmed) {
		boolean overlaps = false;
		boolean blocked = false;
		for (Reservation existing : confirmed) {
			if (ScheduleConflict.overlaps(existing.startsAt(), existing.durationHours(), startsAt, durationHours)) {
				overlaps = true;
			}
			if (ScheduleConflict.conflicts(existing.startsAt(), existing.durationHours(), startsAt, durationHours)) {
				blocked = true;
			}
		}
		if (overlaps) {
			return new AvailableStart(startsAt, SlotState.OCCUPIED);
		}
		if (blocked) {
			return new AvailableStart(startsAt, SlotState.BLOCKED);
		}
		return new AvailableStart(startsAt, SlotState.FREE);
	}

}
