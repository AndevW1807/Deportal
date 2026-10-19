package com.deportal.infrastructure.web.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

import com.deportal.application.reservation.OwnedReservation;
import com.deportal.application.reservation.ReservationDetails;
import com.deportal.domain.reservation.Reservation;

public record ReservationResponse(Long id, Long courtId, String courtName, String date, String startsAt,
		int durationHours, String endsAt, String status, String hourlyRate, String discountRate, String subtotal,
		String discountAmount, String total, String refundAmount, Boolean cancellable) {

	private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH:mm");

	static ReservationResponse from(ReservationDetails details) {
		return from(details, details.reservation().refundAmount(), null);
	}

	static ReservationResponse from(OwnedReservation owned) {
		return from(owned.details(), owned.refundAmount(), owned.cancellable());
	}

	private static ReservationResponse from(ReservationDetails details, BigDecimal refund, Boolean cancellable) {
		Reservation reservation = details.reservation();
		BigDecimal subtotal = reservation.subtotal();
		BigDecimal discount = reservation.discountAmount();
		String endsAt = HOUR.format(reservation.endsAt());
		return new ReservationResponse(reservation.id(), reservation.courtId(), details.courtName(),
				reservation.date().toString(), HOUR.format(reservation.startsAt()), reservation.durationHours(), endsAt,
				reservation.status().name(), reservation.hourlyRate().toPlainString(),
				reservation.discountRate().toPlainString(), subtotal.setScale(2, RoundingMode.HALF_UP).toPlainString(),
				discount.toPlainString(), reservation.total().toPlainString(), money(refund), cancellable);
	}

	private static String money(BigDecimal amount) {
		return amount == null ? null : amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}

}
