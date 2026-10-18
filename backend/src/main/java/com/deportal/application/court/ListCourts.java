package com.deportal.application.court;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.deportal.domain.court.CourtRepository;
import com.deportal.domain.court.SportType;
import com.deportal.domain.court.SportTypeCatalog;

public final class ListCourts {

	private final CourtRepository courts;
	private final SportTypeCatalog sports;

	public ListCourts(CourtRepository courts, SportTypeCatalog sports) {
		this.courts = courts;
		this.sports = sports;
	}

	public List<CourtDetails> execute() {
		Map<String, SportType> byCode = sports.findActive()
			.stream()
			.collect(Collectors.toMap(sport -> sport.code(), Function.identity()));
		return courts.findActive()
			.stream()
			.map(court -> new CourtDetails(court, sportOf(byCode, court.sportTypeCode())))
			.sorted(Comparator.comparing(details -> details.court().name(), String.CASE_INSENSITIVE_ORDER))
			.toList();
	}

	private static SportType sportOf(Map<String, SportType> byCode, String code) {
		SportType known = byCode.get(code);
		if (known != null) {
			return known;
		}
		return new SportType(code, code);
	}

}
