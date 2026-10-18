package com.deportal.infrastructure.web.court;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import com.deportal.application.auth.AuthenticatedUser;
import com.deportal.application.court.CourtDetails;
import com.deportal.application.court.DeleteCourt;
import com.deportal.application.court.ListCourts;
import com.deportal.application.court.ListSportTypes;
import com.deportal.application.court.UpdateCourt;
import com.deportal.domain.court.Court;
import com.deportal.domain.court.SportType;
import com.deportal.domain.court.failure.CourtHasUpcomingReservations;
import com.deportal.domain.court.failure.CourtNameAlreadyUsed;
import com.deportal.domain.court.failure.CourtNotFound;
import com.deportal.domain.court.failure.InvalidCourt;
import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserRole;
import com.deportal.infrastructure.security.SecurityConfig;
import com.deportal.infrastructure.security.TokenIssuer;
import com.deportal.infrastructure.web.auth.AuthExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CourtController.class)
@Import({ SecurityConfig.class, TokenIssuer.class, CourtExceptionHandler.class, AuthExceptionHandler.class })
@TestPropertySource(properties = {
		"jwt.secret=0123456789abcdef0123456789abcdef",
		"jwt.issuer=deportal",
		"jwt.expiration-seconds=900"
})
class CourtControllerTest {

	private static final String COURT = """
			{"name":"Cancha Norte","sportTypeCode":"FOOTBALL","capacity":10,"opensAt":"06:00","closesAt":"22:00","hourlyRate":"20.00"}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TokenIssuer tokens;

	@MockitoBean
	private ListCourts listCourts;

	@MockitoBean
	private ListSportTypes listSportTypes;

	@MockitoBean
	private UpdateCourt updateCourt;

	@MockitoBean
	private DeleteCourt deleteCourt;

	@Test
	void listsActiveCourtsForAnAdmin() throws Exception {
		when(listCourts.execute()).thenReturn(List.of(details()));

		mockMvc.perform(get("/api/admin/courts").header("Authorization", bearer(UserRole.ADMIN)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(4))
			.andExpect(jsonPath("$[0].name").value("Cancha Norte"))
			.andExpect(jsonPath("$[0].sportType.code").value("FOOTBALL"))
			.andExpect(jsonPath("$[0].sportType.name").value("Fútbol"))
			.andExpect(jsonPath("$[0].capacity").value(10))
			.andExpect(jsonPath("$[0].opensAt").value("06:00"))
			.andExpect(jsonPath("$[0].closesAt").value("22:00"))
			.andExpect(jsonPath("$[0].hourlyRate").value("20.00"));
	}

	@Test
	void listsSportTypes() throws Exception {
		when(listSportTypes.execute()).thenReturn(List.of(new SportType("FOOTBALL", "Fútbol")));

		mockMvc.perform(get("/api/admin/sport-types").header("Authorization", bearer(UserRole.ADMIN)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].code").value("FOOTBALL"))
			.andExpect(jsonPath("$[0].name").value("Fútbol"));
	}

	@Test
	void updatesACourt() throws Exception {
		when(updateCourt.execute(eq(4L), eq("Cancha Norte"), eq("FOOTBALL"), eq(10), eq(LocalTime.of(6, 0)),
				eq(LocalTime.of(22, 0)), eq(new BigDecimal("20.00"))))
			.thenReturn(details());

		mockMvc.perform(put("/api/admin/courts/4").header("Authorization", bearer(UserRole.ADMIN))
			.contentType(MediaType.APPLICATION_JSON)
			.content(COURT))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Cancha Norte"));
	}

	@Test
	void deletesACourt() throws Exception {
		mockMvc.perform(delete("/api/admin/courts/4").header("Authorization", bearer(UserRole.ADMIN)))
			.andExpect(status().isNoContent());

		verify(deleteCourt).execute(4L);
	}

	@Test
	void rejectsAnInvalidCourt() throws Exception {
		when(updateCourt.execute(any(), any(), any(), any(Integer.class), any(), any(), any()))
			.thenThrow(new InvalidCourt());

		mockMvc.perform(put("/api/admin/courts/4").header("Authorization", bearer(UserRole.ADMIN))
			.contentType(MediaType.APPLICATION_JSON)
			.content(COURT))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("Invalid request"));
	}

	@Test
	void rejectsACustomer() throws Exception {
		mockMvc.perform(get("/api/admin/courts").header("Authorization", bearer(UserRole.CUSTOMER)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.detail").value("Admin access required"));
	}

	@Test
	void reportsAMissingCourt() throws Exception {
		when(updateCourt.execute(any(), any(), any(), any(Integer.class), any(), any(), any()))
			.thenThrow(new CourtNotFound());

		mockMvc.perform(put("/api/admin/courts/9").header("Authorization", bearer(UserRole.ADMIN))
			.contentType(MediaType.APPLICATION_JSON)
			.content(COURT))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Court not found"));
	}

	@Test
	void reportsADuplicatedName() throws Exception {
		when(updateCourt.execute(any(), any(), any(), any(Integer.class), any(), any(), any()))
			.thenThrow(new CourtNameAlreadyUsed());

		mockMvc.perform(put("/api/admin/courts/4").header("Authorization", bearer(UserRole.ADMIN))
			.contentType(MediaType.APPLICATION_JSON)
			.content(COURT))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.detail").value("Court name already registered"));
	}

	@Test
	void reportsUpcomingReservations() throws Exception {
		org.mockito.Mockito.doThrow(new CourtHasUpcomingReservations()).when(deleteCourt).execute(4L);

		mockMvc.perform(delete("/api/admin/courts/4").header("Authorization", bearer(UserRole.ADMIN)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.detail").value("Court has upcoming reservations"));
	}

	private String bearer(UserRole role) {
		String token = tokens
			.issue(new AuthenticatedUser(1L, "Admin", "admin@admin.com", role, MembershipType.NON_MEMBER))
			.value();
		return "Bearer " + token;
	}

	private static CourtDetails details() {
		Court court = new Court(4L, "Cancha Norte", "FOOTBALL", 10, LocalTime.of(6, 0), LocalTime.of(22, 0),
				new BigDecimal("20.00"), false);
		return new CourtDetails(court, new SportType("FOOTBALL", "Fútbol"));
	}

}
