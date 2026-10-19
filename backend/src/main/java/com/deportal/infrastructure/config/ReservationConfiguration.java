package com.deportal.infrastructure.config;

import java.time.Clock;

import com.deportal.application.reservation.CancelReservation;
import com.deportal.application.reservation.CreateReservation;
import com.deportal.application.reservation.ListAvailability;
import com.deportal.application.reservation.ListMyReservations;
import com.deportal.application.reservation.QuoteReservation;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.reservation.ReservationRepository;
import com.deportal.domain.user.UserRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReservationConfiguration {

	@Bean
	ListAvailability listAvailability(CourtRepository courts, ReservationRepository reservations, Clock clock) {
		return new ListAvailability(courts, reservations, clock);
	}

	@Bean
	CreateReservation createReservation(CourtRepository courts, UserRepository users, ReservationRepository reservations,
			Clock clock) {
		return new CreateReservation(courts, users, reservations, clock);
	}

	@Bean
	QuoteReservation quoteReservation(CourtRepository courts, UserRepository users) {
		return new QuoteReservation(courts, users);
	}

	@Bean
	ListMyReservations listMyReservations(ReservationRepository reservations, CourtRepository courts, Clock clock) {
		return new ListMyReservations(reservations, courts, clock);
	}

	@Bean
	CancelReservation cancelReservation(ReservationRepository reservations, CourtRepository courts, UserRepository users,
			Clock clock) {
		return new CancelReservation(reservations, courts, users, clock);
	}

}
