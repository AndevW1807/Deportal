package com.deportal.domain.court;

import java.math.BigDecimal;

public record CourtReservationUse(Long courtId, int reservations, BigDecimal reservedHours, BigDecimal income) {
}
