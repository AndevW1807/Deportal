package com.deportal.application.court;

import java.util.List;

import com.deportal.domain.court.SportType;
import com.deportal.domain.court.SportTypeCatalog;

public final class ListSportTypes {

	private final SportTypeCatalog sports;

	public ListSportTypes(SportTypeCatalog sports) {
		this.sports = sports;
	}

	public List<SportType> execute() {
		return List.copyOf(sports.findActive());
	}

}
