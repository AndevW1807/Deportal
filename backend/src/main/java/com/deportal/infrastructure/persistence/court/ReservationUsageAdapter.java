package com.deportal.infrastructure.persistence.court;

import java.time.LocalDate;
import java.util.List;

import com.deportal.domain.court.CourtReservationUse;
import com.deportal.domain.court.ReservationUsage;

import org.springframework.stereotype.Repository;

@Repository
public class ReservationUsageAdapter implements ReservationUsage {

	@Override
	public List<CourtReservationUse> between(LocalDate from, LocalDate to) {
		return List.of();
	}

}
