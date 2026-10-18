export interface SportType {
  code: string;
  name: string;
}

export interface Court {
  id: number;
  name: string;
  sportType: SportType;
  capacity: number;
  opensAt: string;
  closesAt: string;
  hourlyRate: string;
}

export interface CourtDraft {
  name: string;
  sportTypeCode: string;
  capacity: number;
  opensAt: string;
  closesAt: string;
  hourlyRate: string;
}

export interface SportLook {
  icon: string;
  tint: string;
  ink: string;
}

const DAY_START = 6 * 60;
const DAY_SPAN = 16 * 60;

export function sportLook(sport: SportType): SportLook {
  const key = `${sport.code} ${sport.name}`
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase();

  if (key.includes('futbol') || key.includes('football') || key.includes('soccer')) {
    return { icon: 'sports_soccer', tint: '#e7f6ec', ink: '#0f6b3c' };
  }

  if (key.includes('basquet') || key.includes('basket')) {
    return { icon: 'sports_basketball', tint: '#fff1e4', ink: '#b45309' };
  }

  if (key.includes('tenis') || key.includes('tennis')) {
    return { icon: 'sports_tennis', tint: '#e7f7f4', ink: '#0f766e' };
  }

  if (key.includes('volei') || key.includes('volley')) {
    return { icon: 'sports_volleyball', tint: '#eef2ff', ink: '#3730a3' };
  }

  return { icon: 'stadium', tint: '#f4f1ea', ink: '#57534e' };
}

export function openSpan(opensAt: string, closesAt: string): { left: string; width: string } {
  const opens = minutes(opensAt);
  const closes = minutes(closesAt);
  const left = ((opens - DAY_START) / DAY_SPAN) * 100;
  const width = ((closes - opens) / DAY_SPAN) * 100;

  return {
    left: `${clamp(left)}%`,
    width: `${clamp(width)}%`,
  };
}

const cop = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

export function formatCop(amount: number): string {
  return cop.format(amount);
}

export function formatRate(hourlyRate: string): string {
  const amount = Number(hourlyRate);
  const text = Number.isFinite(amount) ? formatCop(amount) : hourlyRate;
  return `${text} / hora`;
}

export function capacityLabel(capacity: number): string {
  return capacity === 1 ? '1 persona' : `${capacity} personas`;
}

function minutes(value: string): number {
  const [hour, minute] = value.split(':');
  return Number(hour) * 60 + Number(minute ?? 0);
}

function clamp(value: number): number {
  return Math.min(100, Math.max(0, value));
}
