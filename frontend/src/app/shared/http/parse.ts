export function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

export function text(record: Record<string, unknown>, key: string): string {
  const value = record[key];
  if (typeof value !== 'string' || value.length === 0) {
    throw new Error('Invalid response');
  }

  return value;
}

export function numberOf(record: Record<string, unknown>, key: string): number {
  const value = record[key];
  const amount = typeof value === 'number' ? value : typeof value === 'string' ? Number(value) : Number.NaN;
  if (!Number.isFinite(amount)) {
    throw new Error('Invalid response');
  }

  return amount;
}
