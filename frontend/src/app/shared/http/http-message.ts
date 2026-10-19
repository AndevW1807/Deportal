export function httpMessage(
  error: unknown,
  messages: Readonly<Record<number, string>>,
  fallback = 'No se pudo conectar con el servidor',
): string {
  if (isHttpError(error)) {
    return messages[error.status] ?? fallback;
  }

  return fallback;
}

function isHttpError(error: unknown): error is { status: number } {
  return typeof error === 'object' && error !== null && 'status' in error && typeof error.status === 'number';
}
