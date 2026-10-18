package com.deportal.infrastructure.config;

import com.deportal.application.report.UtilizationReport;
import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.court.ReservationUsage;
import com.deportal.domain.court.SportTypeCatalog;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportConfiguration {

	@Bean
	UtilizationReport utilizationReport(CourtRepository courts, SportTypeCatalog sports, ReservationUsage usage) {
		return new UtilizationReport(courts, sports, usage);
	}

}
