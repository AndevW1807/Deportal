package com.deportal.infrastructure.persistence.reservation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import com.deportal.domain.reservation.Reservation;
import com.deportal.domain.reservation.ReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservations")
public class ReservationJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "court_id", nullable = false)
	private Long courtId;

	@Column(name = "reservation_date", nullable = false)
	private LocalDate reservationDate;

	@Column(name = "starts_at", nullable = false)
	private LocalTime startsAt;

	@Column(name = "ends_at", nullable = false)
	private LocalTime endsAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private ReservationStatus status;

	@Column(name = "base_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal baseAmount;

	@Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal discountAmount;

	@Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
	private BigDecimal amountPaid;

	@Column(name = "refund_amount", precision = 10, scale = 2)
	private BigDecimal refundAmount;

	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected ReservationJpaEntity() {
	}

	static ReservationJpaEntity create(Reservation reservation, LocalDateTime now) {
		ReservationJpaEntity row = new ReservationJpaEntity();
		BigDecimal base = reservation.hourlyRate()
			.multiply(BigDecimal.valueOf(reservation.durationHours()))
			.setScale(2, RoundingMode.HALF_UP);
		row.userId = reservation.customerId();
		row.courtId = reservation.courtId();
		row.reservationDate = reservation.date();
		row.startsAt = reservation.startsAt();
		row.endsAt = reservation.startsAt().plusHours(reservation.durationHours());
		row.status = reservation.status();
		row.baseAmount = base;
		row.discountAmount = base.subtract(reservation.total()).setScale(2, RoundingMode.HALF_UP);
		row.amountPaid = reservation.total();
		row.createdAt = now;
		row.updatedAt = now;
		return row;
	}

	void changeStatus(ReservationStatus status, LocalDateTime now) {
		this.status = status;
		this.updatedAt = now;
		if (status == ReservationStatus.CANCELLED) {
			this.cancelledAt = now;
			this.refundAmount = BigDecimal.ZERO.setScale(2);
		}
	}

	Reservation toDomain() {
		int hours = (int) ChronoUnit.MINUTES.between(startsAt, endsAt) / 60;
		BigDecimal hourly = hours == 0 ? baseAmount
				: baseAmount.divide(BigDecimal.valueOf(hours), 2, RoundingMode.HALF_UP);
		BigDecimal discountRate = baseAmount.signum() == 0 ? BigDecimal.ZERO.setScale(2)
				: discountAmount.divide(baseAmount, 2, RoundingMode.HALF_UP);
		return new Reservation(id, userId, courtId, reservationDate, startsAt, hours, status, hourly, discountRate,
				amountPaid);
	}

}
