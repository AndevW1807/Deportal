package com.deportal.infrastructure.web.court;

import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourtRequest(@NotBlank @Size(max = 120) String name, @NotBlank @Size(max = 32) String sportTypeCode,
		@NotNull @Min(1) @Max(50) Integer capacity, @NotNull LocalTime opensAt, @NotNull LocalTime closesAt,
		@NotNull @DecimalMin("5.00") BigDecimal hourlyRate) {

	public CourtRequest {
		if (name != null) {
			name = name.trim();
		}
		if (sportTypeCode != null) {
			sportTypeCode = sportTypeCode.trim();
		}
	}

}
