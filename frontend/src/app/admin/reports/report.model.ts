import { formatCop } from '../../shared/format/money';
import { isRecord, numberOf, text } from '../../shared/http/parse';

export interface CourtUtilization {
  id: number;
  name: string;
  sportName: string;
  reservations: number;
  reservedHours: number;
  availableHours: number;
  occupancyRate: number;
  income: number;
}

export interface UtilizationReport {
  reservations: number;
  reservedHours: number;
  income: number;
  occupancyRate: number;
  courts: CourtUtilization[];
}

export const emptyReport: UtilizationReport = {
  reservations: 0,
  reservedHours: 0,
  income: 0,
  occupancyRate: 0,
  courts: [],
};

const hours = new Intl.NumberFormat('es-CO', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const percent = new Intl.NumberFormat('es-CO', {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

export function formatHours(value: number): string {
  return `${hours.format(value)} h`;
}

export function formatOccupancy(value: number): string {
  return `${percent.format(value)} %`;
}

export function formatIncome(value: number): string {
  return formatCop(value);
}

export function parseReport(body: unknown): UtilizationReport {
  if (!isRecord(body) || !Array.isArray(body['courts'])) {
    throw new Error('Invalid utilization report');
  }

  return {
    reservations: numberOf(body, 'reservations'),
    reservedHours: numberOf(body, 'reservedHours'),
    income: numberOf(body, 'income'),
    occupancyRate: numberOf(body, 'occupancyRate'),
    courts: body['courts'].map((item) => parseUtilization(item)),
  };
}

function parseUtilization(body: unknown): CourtUtilization {
  if (!isRecord(body)) {
    throw new Error('Invalid utilization report');
  }

  return {
    id: numberOf(body, 'id'),
    name: text(body, 'name'),
    sportName: text(body, 'sportName'),
    reservations: numberOf(body, 'reservations'),
    reservedHours: numberOf(body, 'reservedHours'),
    availableHours: numberOf(body, 'availableHours'),
    occupancyRate: numberOf(body, 'occupancyRate'),
    income: numberOf(body, 'income'),
  };
}
