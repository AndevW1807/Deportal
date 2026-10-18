package com.deportal.infrastructure.persistence.court;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sport_types")
public class SportTypeJpaEntity {

	@Id
	@Column(nullable = false, length = 32)
	private String code;

	@Column(nullable = false, length = 80)
	private String name;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected SportTypeJpaEntity() {
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

}
