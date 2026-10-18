import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../session/auth-service';
import { AuthSession } from '../session/auth-session';
import { SessionUser } from '../session/session.model';

export function viewForRole(role: SessionUser['role']): string {
  return role === 'ADMIN' ? '/admin/courts' : '/';
}

export const activeSessionGuard: CanActivateFn = (_route, state) => {
  const session = inject(AuthSession);
  const router = inject(Router);

  if (!session.token()) {
    return true;
  }

  return inject(AuthService)
    .session()
    .pipe(
      map((user) => {
        const destination = viewForRole(user.role);
        return pathOf(state.url) === destination ? true : router.parseUrl(destination);
      }),
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status === HttpStatusCode.Unauthorized) {
          session.clear();
        }
        return of(true);
      }),
    );
};

function pathOf(url: string): string {
  const path = url.split(/[?#]/)[0] ?? '/';
  if (path.length > 1 && path.endsWith('/')) {
    return path.slice(0, -1);
  }
  return path === '' ? '/' : path;
}
