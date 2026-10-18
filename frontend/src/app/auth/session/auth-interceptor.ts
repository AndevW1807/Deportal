import { HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthSession } from './auth-session';

export function authInterceptor(request: HttpRequest<unknown>, next: HttpHandlerFn) {
  const token = inject(AuthSession).token();
  if (!token) {
    return next(request);
  }

  return next(request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
}
