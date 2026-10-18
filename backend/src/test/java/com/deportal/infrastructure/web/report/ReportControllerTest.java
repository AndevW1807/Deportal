package com.deportal.infrastructure.web.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.deportal.application.auth.AuthenticatedUser;
import com.deportal.application.report.CourtUtilization;
import com.deportal.application.report.UtilizationReport;
import com.deportal.application.report.UtilizationReportView;
import com.deportal.domain.report.failure.InvalidReportPeriod;
import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserRole;
import com.deportal.infrastructure.security.SecurityConfig;
import com.deportal.infrastructure.security.TokenIssuer;
import com.deportal.infrastructure.web.auth.AuthExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportController.class)
@Import({ SecurityConfig.class, TokenIssuer.class, ReportExceptionHandler.class, AuthExceptionHandler.class })
@TestPropertySource(properties = {
		"jwt.secret=0123456789abcdef0123456789abcdef",
		"jwt.issuer=deportal",
		"jwt.expiration-seconds=900"
})
class ReportControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TokenIssuer tokens;

	@MockitoBean
	private UtilizationReport utilizationReport;

	@Test
	void returnsTheUtilizationReport() throws Exception {
		when(utilizationReport.execute(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), "FOOTBALL"))
			.thenReturn(new UtilizationReportView(0, new BigDecimal("0.00"), new BigDecimal("0.00"),
					new BigDecimal("0.00"),
					List.of(new CourtUtilization(1L, "Norte", "Fútbol", 0, new BigDecimal("0.00"),
							new BigDecimal("496.00"), new BigDecimal("0.00"), new BigDecimal("0.00")))));

		mockMvc
			.perform(get("/api/admin/reports/utilization").header("Authorization", bearer(UserRole.ADMIN))
				.param("from", "2026-10-01")
				.param("to", "2026-10-31")
				.param("sportTypeCode", "FOOTBALL"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.reservations").value(0))
			.andExpect(jsonPath("$.occupancyRate").value(0.00))
			.andExpect(jsonPath("$.courts[0].name").value("Norte"))
			.andExpect(jsonPath("$.courts[0].availableHours").value(496.00));
	}

	@Test
	void rejectsAnInvertedRange() throws Exception {
		when(utilizationReport.execute(LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 1), null))
			.thenThrow(new InvalidReportPeriod());

		mockMvc
			.perform(get("/api/admin/reports/utilization").header("Authorization", bearer(UserRole.ADMIN))
				.param("from", "2026-10-31")
				.param("to", "2026-10-01"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("Invalid request"));
	}

	@Test
	void rejectsACustomer() throws Exception {
		mockMvc
			.perform(get("/api/admin/reports/utilization").header("Authorization", bearer(UserRole.CUSTOMER))
				.param("from", "2026-10-01")
				.param("to", "2026-10-31"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.detail").value("Admin access required"));
	}

	private String bearer(UserRole role) {
		String token = tokens
			.issue(new AuthenticatedUser(1L, "Admin", "admin@admin.com", role, MembershipType.NON_MEMBER))
			.value();
		return "Bearer " + token;
	}

}
