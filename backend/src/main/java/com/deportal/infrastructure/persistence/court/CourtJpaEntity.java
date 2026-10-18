package com.deportal.infrastructure.persistence.court;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.deportal.domain.court.Court;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courts")
public class CourtJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "sport_type_code", nullable = false, length = 32)
	private String sportTypeCode;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false)
	private short capacity;

	@Column(name = "opens_at", nullable = false)
	private LocalTime opensAt;

	@Column(name = "closes_at", nullable = false)
	private LocalTime closesAt;

	@Column(name = "hourly_rate", nullable = false, precision = 10, scale = 2)
	private BigDecimal hourlyRate;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected CourtJpaEntity() {
	}

	static CourtJpaEntity from(Court court) {
		CourtJpaEntity row = new CourtJpaEntity();
		row.id = court.id();
		row.sportTypeCode = court.sportTypeCode();
		row.name = court.name();
		row.capacity = (short) court.capacity();
		row.opensAt = court.opensAt();
		row.closesAt = court.closesAt();
		row.hourlyRate = court.hourlyRate();
		return row;
	}

	void markDeleted(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	Court toDomain() {
		return new Court(id, name, sportTypeCode, capacity, opensAt, closesAt, hourlyRate, deletedAt != null);
	}

}
