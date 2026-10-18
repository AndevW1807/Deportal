package com.deportal.domain.reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class WaitlistPriority {

	private WaitlistPriority() {
	}

	public static List<Reservation> order(List<Reservation> waiting, Set<Long> memberIds) {
		List<Reservation> members = new ArrayList<>();
		List<Reservation> guests = new ArrayList<>();
		for (Reservation reservation : waiting) {
			if (memberIds.contains(reservation.customerId())) {
				members.add(reservation);
			}
			else {
				guests.add(reservation);
			}
		}
		members.addAll(guests);
		return members;
	}

}
