package com.deportal.domain.reservation;

public final class ReservationPlacement {

	private ReservationPlacement() {
	}

	public static ReservationDecision decide(boolean sameDay, boolean overlapsConfirmed, boolean blockedByCleaning) {
		if (!overlapsConfirmed && !blockedByCleaning) {
			return ReservationDecision.CONFIRM;
		}
		if (!overlapsConfirmed) {
			return ReservationDecision.REJECT;
		}
		if (sameDay) {
			return ReservationDecision.WAIT;
		}
		return ReservationDecision.REJECT;
	}

}
