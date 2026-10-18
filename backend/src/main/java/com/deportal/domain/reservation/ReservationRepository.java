package com.deportal.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

	Reservation save(Reservation reservation);

	List<Reservation> saveAll(List<Reservation> reservations);

	List<Reservation> findConfirmed(Long courtId, LocalDate date);

	List<Reservation> findWaiting(Long courtId, LocalDate date);

	List<Reservation> findActiveByCustomer(Long customerId);

	Optional<Reservation> findActiveById(Long id);

}
