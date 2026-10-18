package com.deportal.infrastructure.persistence.court;

import java.time.LocalDate;

import com.deportal.domain.court.UpcomingReservations;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UpcomingReservationsAdapter implements UpcomingReservations {

	private final JdbcTemplate jdbc;

	public UpcomingReservationsAdapter(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public boolean existsForCourt(Long courtId, LocalDate today) {
		Integer count = jdbc.queryForObject("""
				SELECT COUNT(*)
				FROM reservations
				WHERE court_id = ?
					AND reservation_date >= ?
					AND status IN ('CONFIRMED', 'WAITING')
					AND deleted_at IS NULL
				""", Integer.class, courtId, today);
		return count != null && count > 0;
	}

}
