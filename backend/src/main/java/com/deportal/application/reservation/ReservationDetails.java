package com.deportal.application.reservation;

import com.deportal.domain.reservation.Reservation;

public record ReservationDetails(Reservation reservation, String courtName) {
}
