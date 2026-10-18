package com.deportal.domain.court;

import java.time.LocalDate;

public interface UpcomingReservations {

	boolean existsForCourt(Long courtId, LocalDate today);

}
