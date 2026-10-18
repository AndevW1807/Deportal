import { Service, signal } from '@angular/core';

const TOKEN_KEY = 'deportal.accessToken';

@Service()
export class AuthSession {
  private readonly tokenSignal = signal<string | null>(readStoredToken());

  readonly token = this.tokenSignal.asReadonly();

  save(accessToken: string): void {
    localStorage.removeItem(TOKEN_KEY);
    sessionStorage.setItem(TOKEN_KEY, accessToken);
    this.tokenSignal.set(accessToken);
  }

  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(TOKEN_KEY);
    this.tokenSignal.set(null);
  }
}

function readStoredToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY) ?? localStorage.getItem(TOKEN_KEY);
}
