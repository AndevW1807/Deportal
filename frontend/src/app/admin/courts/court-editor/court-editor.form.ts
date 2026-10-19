import { Court, SportType } from '../../../shared/court/court.model';
import { formatCop } from '../../../shared/format/money';

export interface CourtEditorData {
  court: Court | null;
  sportTypes: SportType[];
  onSaved: (court: Court) => void;
}

export const WINDOW_MESSAGE = 'El horario debe estar entre 06:00 y 22:00';
export const ORDER_MESSAGE = 'El horario de apertura debe ser anterior al horario de cierre';
export const CAPACITY_MESSAGE = 'La capacidad debe estar entre 1 y 50 personas';
export const MAX_HOURLY_RATE = 99999999.99;
export const RATE_MESSAGE = `La tarifa debe ser mayor o igual a ${formatCop(5)}`;

export interface CourtFormModel {
  name: string;
  sportTypeCode: string;
  capacity: number | null;
  opensAt: string;
  closesAt: string;
  hourlyRate: number;
}

export function formModel(data: CourtEditorData): CourtFormModel {
  const court = data.court;
  if (!court) {
    return {
      name: '',
      sportTypeCode: data.sportTypes[0]?.code ?? '',
      capacity: 10,
      opensAt: '06:00',
      closesAt: '22:00',
      hourlyRate: 5,
    };
  }

  return {
    name: court.name,
    sportTypeCode: court.sportType.code,
    capacity: court.capacity,
    opensAt: court.opensAt,
    closesAt: court.closesAt,
    hourlyRate: Number(court.hourlyRate),
  };
}

export function inWindow(value: string): boolean {
  return value >= '06:00' && value <= '22:00';
}

export function acceptsCapacity(value: string): boolean {
  return value === '' || (/^\d{1,2}$/.test(value) && Number(value) <= 50);
}

export function acceptsRate(value: string): boolean {
  return value === '' || /^\d{1,8}(\.\d{0,2})?$/.test(value);
}

export function restrict(event: InputEvent, accepts: (value: string) => boolean): void {
  if (event.inputType.startsWith('delete') || event.inputType === 'historyUndo' || event.inputType === 'historyRedo') {
    return;
  }

  const input = event.target;
  if (!(input instanceof HTMLInputElement)) {
    return;
  }

  const raw = event.data ?? '';
  const data = raw.replaceAll(',', '.');
  const next = proposed(input, data);
  if (!accepts(next)) {
    event.preventDefault();
    return;
  }

  if (!raw.includes(',')) {
    return;
  }

  event.preventDefault();
  const cursor = (input.selectionStart ?? input.value.length) + data.length;
  input.value = next;
  try {
    input.setSelectionRange(cursor, cursor);
  } catch {
    // Some browsers hide the caret on number inputs.
  }
  input.dispatchEvent(new Event('input', { bubbles: true }));
}

function proposed(input: HTMLInputElement, data: string): string {
  let start = input.value.length;
  let end = start;
  try {
    start = input.selectionStart ?? start;
    end = input.selectionEnd ?? start;
  } catch {
    // Some browsers hide the caret on number inputs.
  }

  return `${input.value.slice(0, start)}${data}${input.value.slice(end)}`;
}
