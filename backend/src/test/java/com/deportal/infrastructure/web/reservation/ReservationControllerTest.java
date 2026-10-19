package com.deportal.infrastructure.web.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.deportal.application.auth.AuthenticatedUser;
import com.deportal.application.court.CourtDetails;
import com.deportal.application.court.ListCourts;
import com.deportal.application.reservation.AvailableStart;
import com.deportal.application.reservation.CancelReservation;
import com.deportal.application.reservation.CreateReservation;
import com.deportal.application.reservation.ListAvailability;
import com.deportal.application.reservation.ListMyReservations;
import com.deportal.application.reservation.QuoteReservation;
import com.deportal.domain.reservation.PriceQuote;
import com.deportal.application.reservation.ReservationDetails;
import com.deportal.domain.court.Court;
import com.deportal.domain.court.SportType;
import com.deportal.domain.reservation.Reservation;
import com.deportal.domain.reservation.ReservationStatus;
import com.deportal.domain.user.MembershipType;
import com.deportal.domain.user.UserRole;
import com.deportal.infrastructure.security.SecurityConfig;
import com.deportal.infrastructure.security.TokenIssuer;
import com.deportal.infrastructure.web.auth.AuthExceptionHandler;
import com.deportal.infrastructure.web.court.CourtExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReservationController.class)
@Import({ SecurityConfig.class, TokenIssuer.class, ReservationExceptionHandler.class, CourtExceptionHandler.class,
		AuthExceptionHandler.class })
@TestPropertySource(properties = {
		"jwt.secret=0123456789abcdef0123456789abcdef",
		"jwt.issuer=deportal",
		"jwt.expiration-seconds=900"
})
class ReservationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TokenIssuer tokens;

	@MockitoBean
	private ListCourts listCourts;

	@MockitoBean
	private ListAvailability listAvailability;

	@MockitoBean
	private CreateReservation createReservation;

	@MockitoBean
	private ListMyReservations listMyReservations;

	@MockitoBean
	private CancelReservation cancelReservation;

	@MockitoBean
	private QuoteReservation quoteReservation;

	@Test
	void listsBookableCourtsForACustomer() throws Exception {
		Court court = new Court(4L, "Norte", "FOOTBALL", 10, LocalTime.of(6, 0), LocalTime.of(22, 0),
				new BigDecimal("20.00"), false);
		when(listCourts.execute()).thenReturn(List.of(new CourtDetails(court, new SportType("FOOTBALL", "Fútbol"))));

		mockMvc.perform(get("/api/reservations/courts").header("Authorization", bearer(UserRole.CUSTOMER)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].name").value("Norte"))
			.andExpect(jsonPath("$[0].sportType.name").value("Fútbol"));
	}

	@Test
	void rejectsAnAdmin() throws Exception {
		mockMvc.perform(get("/api/reservations/courts").header("Authorization", bearer(UserRole.ADMIN)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.detail").value("Customer access required"));
	}

	@Test
	void createsAReservation() throws Exception {
		Reservation reservation = new Reservation(9L, 1L, 4L, LocalDate.of(2026, 10, 19), LocalTime.of(16, 0), 2,
				ReservationStatus.CONFIRMED, new BigDecimal("20.00"), new BigDecimal("0.10"), new BigDecimal("36.00"),
				null);
		when(createReservation.execute(1L, 4L, LocalDate.of(2026, 10, 19), LocalTime.of(16, 0), 2))
			.thenReturn(new ReservationDetails(reservation, "Norte"));
		when(listAvailability.execute(4L, LocalDate.of(2026, 10, 19), 2))
			.thenReturn(List.of(new AvailableStart(LocalTime.of(16, 0), AvailableStart.SlotState.FREE)));

		mockMvc
			.perform(get("/api/reservations/availability").header("Authorization", bearer(UserRole.CUSTOMER))
				.param("courtId", "4")
				.param("date", "2026-10-19")
				.param("durationHours", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].state").value("FREE"));

		mockMvc
			.perform(post("/api/reservations").header("Authorization", bearer(UserRole.CUSTOMER))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"courtId":4,"date":"2026-10-19","startsAt":"16:00","durationHours":2}\
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("CONFIRMED"))
			.andExpect(jsonPath("$.total").value("36.00"))
			.andExpect(jsonPath("$.endsAt").value("18:00"));
	}

	@Test
	void quotesTheSequentialDiscounts() throws Exception {
		when(quoteReservation.execute(1L, 4L, LocalTime.of(8, 0), 2)).thenReturn(new PriceQuote(new BigDecimal("40.00"),
				new BigDecimal("4.00"), new BigDecimal("7.20"), new BigDecimal("28.80")));

		mockMvc
			.perform(get("/api/reservations/quote").header("Authorization", bearer(UserRole.CUSTOMER))
				.param("courtId", "4")
				.param("startsAt", "08:00")
				.param("durationHours", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.subtotal").value("40.00"))
			.andExpect(jsonPath("$.memberDiscount").value("4.00"))
			.andExpect(jsonPath("$.offPeakDiscount").value("7.20"))
			.andExpect(jsonPath("$.total").value("28.80"))
			.andExpect(jsonPath("$.endsAt").value("10:00"));
	}

	@Test
	void cancelsOnlyThroughTheCustomerSession() throws Exception {
		Reservation reservation = new Reservation(9L, 1L, 4L, LocalDate.of(2026, 10, 19), LocalTime.of(16, 0), 1,
				ReservationStatus.CANCELLED, new BigDecimal("20.00"), new BigDecimal("0.00"), new BigDecimal("20.00"),
				new BigDecimal("20.00"));
		when(cancelReservation.execute(1L, 9L)).thenReturn(new ReservationDetails(reservation, "Norte"));

		mockMvc.perform(delete("/api/reservations/9").header("Authorization", bearer(UserRole.CUSTOMER)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CANCELLED"))
			.andExpect(jsonPath("$.refundAmount").value("20.00"));
	}

	private String bearer(UserRole role) {
		String token = tokens
			.issue(new AuthenticatedUser(1L, "Juan", "juan@deportal.test", role, MembershipType.MEMBER))
			.value();
		return "Bearer " + token;
	}

}
