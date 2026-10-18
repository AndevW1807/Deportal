import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { Court, CourtDraft, SportType } from './court.model';

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

export function parseCourts(body: unknown): Court[] {
  if (!Array.isArray(body)) {
    throw new Error('Invalid courts response');
  }

  return body.map((item) => parseCourt(item));
}

export function parseSportTypes(body: unknown): SportType[] {
  if (!Array.isArray(body)) {
    throw new Error('Invalid sport types response');
  }

  return body.map((item) => parseSportType(item));
}

export function parseCourt(body: unknown): Court {
  if (!isRecord(body) || typeof body['id'] !== 'number' || typeof body['name'] !== 'string') {
    throw new Error('Invalid court response');
  }

  const opensAt = clock(body['opensAt']);
  const closesAt = clock(body['closesAt']);
  if (opensAt === null || closesAt === null || typeof body['capacity'] !== 'number') {
    throw new Error('Invalid court response');
  }

  return {
    id: body['id'],
    name: body['name'],
    sportType: parseSportType(body['sportType']),
    capacity: body['capacity'],
    opensAt,
    closesAt,
    hourlyRate: money(body['hourlyRate']),
  };
}

function parseSportType(body: unknown): SportType {
  if (!isRecord(body) || typeof body['code'] !== 'string' || typeof body['name'] !== 'string') {
    throw new Error('Invalid sport type response');
  }

  return { code: body['code'], name: body['name'] };
}

function clock(value: unknown): string | null {
  if (typeof value !== 'string' || !/^\d{2}:\d{2}/.test(value)) {
    return null;
  }

  return value.slice(0, 5);
}

function money(value: unknown): string {
  const amount = typeof value === 'number' ? value : typeof value === 'string' ? Number(value) : NaN;
  if (!Number.isFinite(amount)) {
    throw new Error('Invalid court response');
  }

  return amount.toFixed(2);
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}
