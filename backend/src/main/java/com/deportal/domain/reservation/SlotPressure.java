package com.deportal.domain.reservation;

import java.time.LocalTime;
import java.util.List;

public record SlotPressure(boolean overlaps, boolean blockedByCleaning) {

	public static SlotPressure between(List<Reservation> confirmed, LocalTime startsAt, int durationHours) {
		boolean overlaps = false;
		boolean blockedByCleaning = false;
		for (Reservation existing : confirmed) {
			if (ScheduleConflict.overlaps(existing.startsAt(), existing.durationHours(), startsAt, durationHours)) {
				overlaps = true;
			}
			if (ScheduleConflict.conflicts(existing.startsAt(), existing.durationHours(), startsAt, durationHours)) {
				blockedByCleaning = true;
			}
		}
		return new SlotPressure(overlaps, blockedByCleaning);
	}

}
