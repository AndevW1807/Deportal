import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { CustomerReservation, parseReservation } from './reservation.model';

export interface ReservationDraft {
  courtId: number;
  date: string;
  startsAt: string;
  durationHours: number;
}

@Service()
export class ReservationService {
  private readonly http = inject(HttpClient);

  create(draft: ReservationDraft): Observable<CustomerReservation> {
    return this.http.post<unknown>('/api/reservations', draft).pipe(map((body) => parseReservation(body)));
  }
}
