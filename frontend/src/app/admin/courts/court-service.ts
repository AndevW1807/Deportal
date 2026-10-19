import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { parseCourt, parseCourts, parseSportTypes } from '../../shared/court/parse-court';
import { Court, SportType } from '../../shared/court/court.model';
import { CourtDraft } from './court.model';

@Service()
export class CourtService {
  private readonly http = inject(HttpClient);

  list(): Observable<Court[]> {
    return this.http.get<unknown>('/api/admin/courts').pipe(map((body) => parseCourts(body)));
  }

  sportTypes(): Observable<SportType[]> {
    return this.http
      .get<unknown>('/api/admin/sport-types')
      .pipe(map((body) => parseSportTypes(body)));
  }

  create(draft: CourtDraft): Observable<Court> {
    return this.http.post<unknown>('/api/admin/courts', draft).pipe(map((body) => parseCourt(body)));
  }

  update(id: number, draft: CourtDraft): Observable<Court> {
    return this.http
      .put<unknown>(`/api/admin/courts/${id}`, draft)
      .pipe(map((body) => parseCourt(body)));
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`/api/admin/courts/${id}`);
  }
}
