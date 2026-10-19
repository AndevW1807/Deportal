import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../session/auth-service';
import { AuthSession } from '../session/auth-session';
import { accessTokenExpired } from '../session/token-expiry';

export const signedInGuard: CanActivateFn = () => {
  const session = inject(AuthSession);
  const router = inject(Router);

  if (dropExpired(session) || !session.token()) {
    return router.parseUrl('/login');
  }

  return inject(AuthService)
    .session()
    .pipe(
      map(() => true),
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status === HttpStatusCode.Unauthorized) {
          session.clear();
        }
        return of(router.parseUrl('/login'));
      }),
    );
};

export const adminGuard: CanActivateFn = () => {
  const session = inject(AuthSession);
  const router = inject(Router);

  if (dropExpired(session) || !session.token()) {
    return router.parseUrl('/login');
  }

  return inject(AuthService)
    .session()
    .pipe(
      map((user) => (user.role === 'ADMIN' ? true : router.parseUrl('/app'))),
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status === HttpStatusCode.Unauthorized) {
          session.clear();
        }
        return of(router.parseUrl('/login'));
      }),
    );
};

function dropExpired(session: AuthSession): boolean {
  const token = session.token();
  if (token && accessTokenExpired(token)) {
    session.clear();
    return true;
  }
  return false;
}
