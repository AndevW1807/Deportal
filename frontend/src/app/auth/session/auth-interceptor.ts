import { HttpErrorResponse, HttpHandlerFn, HttpRequest, HttpStatusCode } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthSession } from './auth-session';
import { routeNeedsSession } from './token-expiry';

export function authInterceptor(request: HttpRequest<unknown>, next: HttpHandlerFn) {
  const session = inject(AuthSession);
  const router = inject(Router);
  const token = session.token();
  const outgoing = token
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(outgoing).pipe(
    catchError((error: unknown) => {
      if (
        token &&
        error instanceof HttpErrorResponse &&
        error.status === HttpStatusCode.Unauthorized &&
        routeNeedsSession(router.url)
      ) {
        session.clear();
        void router.navigateByUrl('/login');
      }
      return throwError(() => error);
    }),
  );
}
