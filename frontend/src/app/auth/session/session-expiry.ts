import { Service, effect, inject, untracked } from '@angular/core';
import { Router } from '@angular/router';
import { AuthSession } from './auth-session';
import { routeNeedsSession, tokenExpiresAt } from './token-expiry';

@Service()
export class SessionExpiry {
  private readonly session = inject(AuthSession);
  private readonly router = inject(Router);

  constructor() {
    effect((onCleanup) => {
      const token = this.session.token();
      const expiresAt = token === null ? null : tokenExpiresAt(token);
      if (expiresAt === null) {
        return;
      }

      const delay = Math.max(0, expiresAt - Date.now());
      const timer = setTimeout(() => {
        untracked(() => this.leaveIfSignedIn());
      }, delay);
      onCleanup(() => clearTimeout(timer));
    });
  }

  private leaveIfSignedIn(): void {
    this.session.clear();
    if (routeNeedsSession(this.router.url)) {
      void this.router.navigateByUrl('/login');
    }
  }
}
