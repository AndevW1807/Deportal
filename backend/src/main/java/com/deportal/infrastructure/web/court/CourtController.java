package com.deportal.infrastructure.web.court;

import java.util.List;

import com.deportal.application.court.CreateCourt;
import com.deportal.application.court.DeleteCourt;
import com.deportal.application.court.ListCourts;
import com.deportal.application.court.ListSportTypes;
import com.deportal.application.court.UpdateCourt;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class CourtController {

	private final ListCourts listCourts;
	private final ListSportTypes listSportTypes;
	private final CreateCourt createCourt;
	private final UpdateCourt updateCourt;
	private final DeleteCourt deleteCourt;

	public CourtController(ListCourts listCourts, ListSportTypes listSportTypes, CreateCourt createCourt,
			UpdateCourt updateCourt, DeleteCourt deleteCourt) {
		this.listCourts = listCourts;
		this.listSportTypes = listSportTypes;
		this.createCourt = createCourt;
		this.updateCourt = updateCourt;
		this.deleteCourt = deleteCourt;
	}

	@GetMapping("/sport-types")
	public List<SportTypeResponse> sportTypes() {
		return listSportTypes.execute().stream().map(sport -> new SportTypeResponse(sport.code(), sport.name())).toList();
	}

	@GetMapping("/courts")
	public List<CourtResponse> courts() {
		return listCourts.execute().stream().map(details -> CourtResponse.from(details)).toList();
	}

	@PostMapping("/courts")
	@ResponseStatus(HttpStatus.CREATED)
	public CourtResponse create(@Valid @RequestBody CourtRequest request) {
		return CourtResponse.from(createCourt.execute(request.name(), request.sportTypeCode(), request.capacity(),
				request.opensAt(), request.closesAt(), request.hourlyRate()));
	}

	@PutMapping("/courts/{id}")
	public CourtResponse update(@PathVariable Long id, @Valid @RequestBody CourtRequest request) {
		return CourtResponse.from(updateCourt.execute(id, request.name(), request.sportTypeCode(), request.capacity(),
				request.opensAt(), request.closesAt(), request.hourlyRate()));
	}

	@DeleteMapping("/courts/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		deleteCourt.execute(id);
	}

}
