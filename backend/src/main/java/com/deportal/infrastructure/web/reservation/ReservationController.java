package com.deportal.infrastructure.web.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.deportal.application.auth.failure.InvalidCredentials;
import com.deportal.application.court.ListCourts;
import com.deportal.application.reservation.CancelReservation;
import com.deportal.application.reservation.CreateReservation;
import com.deportal.application.reservation.ListAvailability;
import com.deportal.application.reservation.ListMyReservations;
import com.deportal.application.reservation.QuoteReservation;
import com.deportal.infrastructure.web.court.CourtResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

	private final ListCourts listCourts;
	private final ListAvailability listAvailability;
	private final CreateReservation createReservation;
	private final ListMyReservations listMyReservations;
	private final CancelReservation cancelReservation;

	private final QuoteReservation quoteReservation;

	public ReservationController(ListCourts listCourts, ListAvailability listAvailability,
			CreateReservation createReservation, ListMyReservations listMyReservations,
			CancelReservation cancelReservation, QuoteReservation quoteReservation) {
		this.listCourts = listCourts;
		this.listAvailability = listAvailability;
		this.createReservation = createReservation;
		this.listMyReservations = listMyReservations;
		this.cancelReservation = cancelReservation;
		this.quoteReservation = quoteReservation;
	}

	@GetMapping("/courts")
	public List<CourtResponse> courts() {
		return listCourts.execute().stream().map(CourtResponse::from).toList();
	}

	@GetMapping("/availability")
	public List<AvailableStartResponse> availability(@RequestParam Long courtId, @RequestParam LocalDate date,
			@RequestParam int durationHours) {
		return listAvailability.execute(courtId, date, durationHours).stream().map(AvailableStartResponse::from).toList();
	}

	@GetMapping("/quote")
	public QuoteResponse quote(@AuthenticationPrincipal Jwt jwt, @RequestParam Long courtId,
			@RequestParam LocalTime startsAt, @RequestParam int durationHours) {
		return QuoteResponse.from(quoteReservation.execute(customerId(jwt), courtId, startsAt, durationHours), startsAt,
				durationHours);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ReservationResponse create(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody CreateReservationRequest request) {
		return ReservationResponse.from(createReservation.execute(customerId(jwt), request.courtId(), request.date(),
				request.startsAt(), request.durationHours()));
	}

	@GetMapping
	public List<ReservationResponse> mine(@AuthenticationPrincipal Jwt jwt) {
		return listMyReservations.execute(customerId(jwt)).stream().map(ReservationResponse::from).toList();
	}

	@DeleteMapping("/{id}")
	public ReservationResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return ReservationResponse.from(cancelReservation.execute(customerId(jwt), id));
	}

	private static long customerId(Jwt jwt) {
		if (jwt == null || jwt.getSubject() == null) {
			throw new InvalidCredentials();
		}
		try {
			return Long.parseLong(jwt.getSubject());
		}
		catch (NumberFormatException exception) {
			throw new InvalidCredentials();
		}
	}

}
