package com.deportal.infrastructure.config;

import java.time.Clock;

import com.deportal.application.court.CreateCourt;
import com.deportal.application.court.DeleteCourt;
import com.deportal.application.court.ListCourts;
import com.deportal.application.court.ListSportTypes;
import com.deportal.application.court.UpdateCourt;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.court.SportTypeCatalog;
import com.deportal.domain.court.UpcomingReservations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CourtConfiguration {

	@Bean
	ListCourts listCourts(CourtRepository courts, SportTypeCatalog sports) {
		return new ListCourts(courts, sports);
	}

	@Bean
	ListSportTypes listSportTypes(SportTypeCatalog sports) {
		return new ListSportTypes(sports);
	}

	@Bean
	CreateCourt createCourt(CourtRepository courts, SportTypeCatalog sports) {
		return new CreateCourt(courts, sports);
	}

	@Bean
	UpdateCourt updateCourt(CourtRepository courts, SportTypeCatalog sports) {
		return new UpdateCourt(courts, sports);
	}

	@Bean
	DeleteCourt deleteCourt(CourtRepository courts, UpcomingReservations upcoming, Clock clock) {
		return new DeleteCourt(courts, upcoming, clock);
	}

}
