import { isRecord, numberOf, text } from '../../shared/http/parse';

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
  refundAmount?: string;
}

export interface MyReservation {
  id: number;
  courtName: string;
  date: string;
  startsAt: string;
  endsAt: string;
  durationHours: number;
  status: 'CONFIRMED' | 'WAITING';
  total: string;
  cancellable: boolean;
  refundAmount: string | null;
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

export function parseQuote(body: unknown): Quote {
  const record = objectOf(body);
  return {
    subtotal: moneyOf(record, 'subtotal'),
    memberDiscount: moneyOf(record, 'memberDiscount'),
    offPeakDiscount: moneyOf(record, 'offPeakDiscount'),
    total: moneyOf(record, 'total'),
    endsAt: text(record, 'endsAt'),
  };
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
    refundAmount: typeof record['refundAmount'] === 'string' ? record['refundAmount'] : undefined,
  };
}

export function parseReservations(body: unknown): MyReservation[] {
  if (!Array.isArray(body)) {
    throw new Error('Invalid reservations response');
  }

  return body.map((item) => parseMine(item));
}

export function reservationStatusLabel(status: MyReservation['status']): string {
  return status === 'WAITING' ? 'En lista de espera' : 'Confirmada';
}

function parseMine(body: unknown): MyReservation {
  const record = objectOf(body);
  const status = record['status'];
  if (status !== 'CONFIRMED' && status !== 'WAITING') {
    throw new Error('Invalid reservations response');
  }

  const refund = record['refundAmount'];
  return {
    id: numberOf(record, 'id'),
    courtName: text(record, 'courtName'),
    date: text(record, 'date'),
    startsAt: text(record, 'startsAt'),
    endsAt: text(record, 'endsAt'),
    durationHours: numberOf(record, 'durationHours'),
    status,
    total: text(record, 'total'),
    cancellable: record['cancellable'] === true,
    refundAmount: typeof refund === 'string' ? refund : null,
  };
}

function pad(value: number): string {
  return String(value).padStart(2, '0');
}

function objectOf(value: unknown): Record<string, unknown> {
  if (!isRecord(value)) {
    throw new Error('Invalid response');
  }

  return value;
}

function moneyOf(record: Record<string, unknown>, key: string): number {
  const amount = Number(text(record, key));
  if (!Number.isFinite(amount)) {
    throw new Error('Invalid response');
  }

  return amount;
}
