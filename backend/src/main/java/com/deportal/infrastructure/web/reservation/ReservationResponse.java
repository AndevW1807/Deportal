package com.deportal.infrastructure.web.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

import com.deportal.application.reservation.ReservationDetails;
import com.deportal.domain.reservation.Reservation;

public record ReservationResponse(Long id, Long courtId, String courtName, String date, String startsAt,
		int durationHours, String endsAt, String status, String hourlyRate, String discountRate, String subtotal,
		String discountAmount, String total) {

	private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH:mm");

	static ReservationResponse from(ReservationDetails details) {
		Reservation reservation = details.reservation();
		BigDecimal subtotal = reservation.hourlyRate().multiply(BigDecimal.valueOf(reservation.durationHours()));
		BigDecimal discount = subtotal.subtract(reservation.total()).setScale(2, RoundingMode.HALF_UP);
		int end = reservation.startsAt().getHour() * 60 + reservation.startsAt().getMinute()
				+ reservation.durationHours() * 60;
		String endsAt = String.format("%02d:%02d", end / 60, end % 60);
		return new ReservationResponse(reservation.id(), reservation.courtId(), details.courtName(),
				reservation.date().toString(), HOUR.format(reservation.startsAt()), reservation.durationHours(), endsAt,
				reservation.status().name(), reservation.hourlyRate().toPlainString(),
				reservation.discountRate().toPlainString(), subtotal.setScale(2, RoundingMode.HALF_UP).toPlainString(),
				discount.toPlainString(), reservation.total().toPlainString());
	}

}
