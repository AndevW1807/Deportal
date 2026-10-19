export function tokenExpiresAt(token: string): number | null {
  const payload = token.split('.')[1];
  if (!payload) {
    return null;
  }

  try {
    const decoded = JSON.parse(decodeBase64Url(payload)) as unknown;
    if (
      typeof decoded !== 'object' ||
      decoded === null ||
      !('exp' in decoded) ||
      typeof decoded.exp !== 'number'
    ) {
      return null;
    }
    return decoded.exp * 1000;
  } catch {
    return null;
  }
}

export function accessTokenExpired(token: string, now = Date.now()): boolean {
  const expiresAt = tokenExpiresAt(token);
  return expiresAt !== null && expiresAt <= now;
}

export function routeNeedsSession(url: string): boolean {
  const path = url.split(/[?#]/)[0] ?? '/';
  return path === '/app' || path.startsWith('/app/') || path === '/admin' || path.startsWith('/admin/');
}

function decodeBase64Url(value: string): string {
  const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
  return atob(padded);
}
