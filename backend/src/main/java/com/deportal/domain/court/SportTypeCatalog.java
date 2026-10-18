package com.deportal.domain.court;

import java.util.List;

public interface SportTypeCatalog {

	List<SportType> findActive();

	boolean exists(String code);

}
