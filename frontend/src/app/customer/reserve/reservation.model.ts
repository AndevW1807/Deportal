import { Court } from '../../admin/courts/court.model';

export interface Slot {
  startsAt: string;
  state: 'FREE' | 'OCCUPIED' | 'BLOCKED';
}

export interface CustomerReservation {
  id: number;
  courtId: number;
  courtName: string;
  date: string;
  startsAt: string;
  durationHours: number;
  endsAt: string;
  status: 'CONFIRMED' | 'WAITING' | 'CANCELLED';
  hourlyRate: string;
  discountRate: string;
  subtotal: string;
  discountAmount: string;
  total: string;
}

export interface SessionMembership {
  type: 'MEMBER' | 'NON_MEMBER';
}

export interface Quote {
  subtotal: number;
  memberDiscount: number;
  offPeakDiscount: number;
  total: number;
  endsAt: string;
}

export function bogotaToday(now = new Date()): string {
  return new Intl.DateTimeFormat('en-CA', {
    timeZone: 'America/Bogota',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(now);
}

export function formatDay(iso: string): string {
  return new Intl.DateTimeFormat('es-CO', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC',
  }).format(new Date(`${iso}T00:00:00Z`));
}

export function monthTitle(year: number, month: number): string {
  const label = new Intl.DateTimeFormat('es-CO', {
    month: 'long',
    year: 'numeric',
    timeZone: 'UTC',
  }).format(new Date(Date.UTC(year, month - 1, 1)));
  return label.charAt(0).toLocaleUpperCase('es') + label.slice(1);
}

export function monthCells(year: number, month: number): ({ iso: string; day: number } | null)[] {
  const first = new Date(Date.UTC(year, month - 1, 1));
  const leading = (first.getUTCDay() + 6) % 7;
  const count = new Date(Date.UTC(year, month, 0)).getUTCDate();
  const cells: ({ iso: string; day: number } | null)[] = Array.from({ length: leading }, () => null);

  for (let day = 1; day <= count; day += 1) {
    cells.push({ iso: `${year}-${pad(month)}-${pad(day)}`, day });
  }

  return cells;
}

export function shiftMonth(year: number, month: number, delta: number): { year: number; month: number } {
  const next = new Date(Date.UTC(year, month - 1 + delta, 1));
  return { year: next.getUTCFullYear(), month: next.getUTCMonth() + 1 };
}

export function quoteFor(court: Court, hours: number, member: boolean, startsAt: string): Quote {
  const subtotalCents = Math.round(Number(court.hourlyRate) * 100) * hours;
  const memberCents = member ? Math.round(subtotalCents * 0.1) : 0;
  return {
    subtotal: subtotalCents / 100,
    memberDiscount: memberCents / 100,
    offPeakDiscount: 0,
    total: (subtotalCents - memberCents) / 100,
    endsAt: addHours(startsAt, hours),
  };
}

export function parseCourts(body: unknown): Court[] {
  if (!Array.isArray(body)) {
    throw new Error('Invalid courts response');
  }

  return body.map((item) => parseCourt(item));
}

export function parseSlots(body: unknown): Slot[] {
  if (!Array.isArray(body)) {
    throw new Error('Invalid availability response');
  }

  return body.map((item) => {
    const record = objectOf(item);
    const state = record['state'];
    if (state !== 'FREE' && state !== 'OCCUPIED' && state !== 'BLOCKED') {
      throw new Error('Invalid availability response');
    }

    return { startsAt: text(record, 'startsAt'), state };
  });
}

export function parseReservation(body: unknown): CustomerReservation {
  const record = objectOf(body);
  const status = record['status'];
  if (status !== 'CONFIRMED' && status !== 'WAITING' && status !== 'CANCELLED') {
    throw new Error('Invalid reservation response');
  }

  return {
    id: numberOf(record, 'id'),
    courtId: numberOf(record, 'courtId'),
    courtName: text(record, 'courtName'),
    date: text(record, 'date'),
    startsAt: text(record, 'startsAt'),
    durationHours: numberOf(record, 'durationHours'),
    endsAt: text(record, 'endsAt'),
    status,
    hourlyRate: text(record, 'hourlyRate'),
    discountRate: text(record, 'discountRate'),
    subtotal: text(record, 'subtotal'),
    discountAmount: text(record, 'discountAmount'),
    total: text(record, 'total'),
  };
}

export function parseMembership(body: unknown): SessionMembership {
  const record = objectOf(body);
  return { type: record['type'] === 'MEMBER' ? 'MEMBER' : 'NON_MEMBER' };
}

function parseCourt(body: unknown): Court {
  const record = objectOf(body);
  const sport = objectOf(record['sportType']);
  return {
    id: numberOf(record, 'id'),
    name: text(record, 'name'),
    sportType: { code: text(sport, 'code'), name: text(sport, 'name') },
    capacity: numberOf(record, 'capacity'),
    opensAt: text(record, 'opensAt'),
    closesAt: text(record, 'closesAt'),
    hourlyRate: text(record, 'hourlyRate'),
  };
}

function addHours(startsAt: string, hours: number): string {
  const [hour, minute] = startsAt.split(':');
  const total = Number(hour) * 60 + Number(minute ?? 0) + hours * 60;
  return `${pad(Math.floor(total / 60))}:${pad(total % 60)}`;
}

function pad(value: number): string {
  return String(value).padStart(2, '0');
}

function objectOf(value: unknown): Record<string, unknown> {
  if (typeof value !== 'object' || value === null) {
    throw new Error('Invalid response');
  }

  return value as Record<string, unknown>;
}

function text(record: Record<string, unknown>, key: string): string {
  const value = record[key];
  if (typeof value !== 'string' || value.length === 0) {
    throw new Error('Invalid response');
  }

  return value;
}

function numberOf(record: Record<string, unknown>, key: string): number {
  const value = record[key];
  if (typeof value !== 'number' || !Number.isFinite(value)) {
    throw new Error('Invalid response');
  }

  return value;
}
