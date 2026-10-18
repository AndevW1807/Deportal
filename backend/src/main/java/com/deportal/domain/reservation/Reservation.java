package com.deportal.domain.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public final class Reservation {

	private final Long id;
	private final Long customerId;
	private final Long courtId;
	private final LocalDate date;
	private final LocalTime startsAt;
	private final int durationHours;
	private final ReservationStatus status;
	private final BigDecimal hourlyRate;
	private final BigDecimal discountRate;
	private final BigDecimal total;

	public Reservation(Long id, Long customerId, Long courtId, LocalDate date, LocalTime startsAt, int durationHours,
			ReservationStatus status, BigDecimal hourlyRate, BigDecimal discountRate, BigDecimal total) {
		this.id = id;
		this.customerId = customerId;
		this.courtId = courtId;
		this.date = date;
		this.startsAt = startsAt;
		this.durationHours = durationHours;
		this.status = status;
		this.hourlyRate = hourlyRate;
		this.discountRate = discountRate;
		this.total = total;
	}

	public Reservation withStatus(ReservationStatus status) {
		return new Reservation(id, customerId, courtId, date, startsAt, durationHours, status, hourlyRate, discountRate,
				total);
	}

	public Long id() {
		return id;
	}

	public Long customerId() {
		return customerId;
	}

	public Long courtId() {
		return courtId;
	}

	public LocalDate date() {
		return date;
	}

	public LocalTime startsAt() {
		return startsAt;
	}

	public int durationHours() {
		return durationHours;
	}

	public ReservationStatus status() {
		return status;
	}

	public BigDecimal hourlyRate() {
		return hourlyRate;
	}

	public BigDecimal discountRate() {
		return discountRate;
	}

	public BigDecimal total() {
		return total;
	}

}
