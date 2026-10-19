package com.deportal.domain.reservation;

import java.math.BigDecimal;
import java.time.LocalTime;

public interface DiscountStrategy {

	DiscountKind kind();

	boolean applies(boolean member, LocalTime startsAt);

	BigDecimal rate();

}
