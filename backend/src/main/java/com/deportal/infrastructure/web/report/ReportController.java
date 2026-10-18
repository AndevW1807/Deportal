package com.deportal.infrastructure.web.report;

import java.time.LocalDate;

import com.deportal.application.report.UtilizationReport;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
public class ReportController {

	private final UtilizationReport utilizationReport;

	public ReportController(UtilizationReport utilizationReport) {
		this.utilizationReport = utilizationReport;
	}

	@GetMapping("/utilization")
	public UtilizationReportResponse utilization(@RequestParam LocalDate from, @RequestParam LocalDate to,
			@RequestParam(required = false) String sportTypeCode) {
		return UtilizationReportResponse.from(utilizationReport.execute(from, to, sportTypeCode));
	}

}
