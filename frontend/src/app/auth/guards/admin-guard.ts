import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../session/auth-service';
import { AuthSession } from '../session/auth-session';

export const adminGuard: CanActivateFn = () => {
  const session = inject(AuthSession);
  const router = inject(Router);

  if (!session.token()) {
    return router.parseUrl('/login');
  }

  return inject(AuthService)
    .session()
    .pipe(
      map((user) => (user.role === 'ADMIN' ? true : router.parseUrl('/'))),
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status === HttpStatusCode.Unauthorized) {
          session.clear();
        }
        return of(router.parseUrl('/login'));
      }),
    );
};
