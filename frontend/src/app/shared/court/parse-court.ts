import { Court, SportType } from './court.model';
import { isRecord, numberOf, text } from '../http/parse';

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
  if (!isRecord(body)) {
    throw new Error('Invalid court response');
  }

  const opensAt = clock(body['opensAt']);
  const closesAt = clock(body['closesAt']);
  if (opensAt === null || closesAt === null) {
    throw new Error('Invalid court response');
  }

  return {
    id: numberOf(body, 'id'),
    name: text(body, 'name'),
    sportType: parseSportType(body['sportType']),
    capacity: numberOf(body, 'capacity'),
    opensAt,
    closesAt,
    hourlyRate: money(body['hourlyRate']),
  };
}

function parseSportType(body: unknown): SportType {
  if (!isRecord(body)) {
    throw new Error('Invalid sport type response');
  }

  return { code: text(body, 'code'), name: text(body, 'name') };
}

function clock(value: unknown): string | null {
  if (typeof value !== 'string' || !/^\d{2}:\d{2}/.test(value)) {
    return null;
  }

  return value.slice(0, 5);
}

function money(value: unknown): string {
  const amount = typeof value === 'number' ? value : typeof value === 'string' ? Number(value) : Number.NaN;
  if (!Number.isFinite(amount)) {
    throw new Error('Invalid court response');
  }

  return amount.toFixed(2);
}
