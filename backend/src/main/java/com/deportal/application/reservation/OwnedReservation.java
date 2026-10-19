package com.deportal.application.reservation;

import java.math.BigDecimal;

public record OwnedReservation(ReservationDetails details, boolean cancellable, BigDecimal refundAmount) {
}
