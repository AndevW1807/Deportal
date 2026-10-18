package com.deportal.domain.court;

import java.time.LocalDate;
import java.util.List;

public interface ReservationUsage {

	List<CourtReservationUse> between(LocalDate from, LocalDate to);

}
