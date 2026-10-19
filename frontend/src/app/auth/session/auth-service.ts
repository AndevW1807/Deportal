import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { isRecord } from '../../shared/http/parse';
import { AccessToken, SessionUser } from './session.model';

@Service()
export class AuthService {
  private readonly http = inject(HttpClient);

  login(email: string, password: string): Observable<AccessToken> {
    return this.http
      .post<unknown>('/api/auth/login', { email, password })
      .pipe(map((body) => parseAccessToken(body)));
  }

  register(
    name: string,
    email: string,
    password: string,
    confirmPassword: string,
  ): Observable<SessionUser> {
    return this.http
      .post<unknown>('/api/auth/register', { name, email, password, confirmPassword })
      .pipe(map((body) => parseSessionUser(body)));
  }

  session(): Observable<SessionUser> {
    return this.http.get<unknown>('/api/auth/session').pipe(map((body) => parseSessionUser(body)));
  }
}

function parseAccessToken(body: unknown): AccessToken {
  if (!isRecord(body) || typeof body['accessToken'] !== 'string' || body['accessToken'] === '') {
    throw new Error('Invalid login response');
  }

  return {
    tokenType: typeof body['tokenType'] === 'string' ? body['tokenType'] : 'Bearer',
    accessToken: body['accessToken'],
    expiresIn: typeof body['expiresIn'] === 'number' ? body['expiresIn'] : 0,
  };
}

function parseSessionUser(body: unknown): SessionUser {
  if (!isRecord(body) || (body['role'] !== 'ADMIN' && body['role'] !== 'CUSTOMER')) {
    throw new Error('Invalid session response');
  }

  return {
    id: typeof body['id'] === 'number' ? body['id'] : 0,
    name: typeof body['name'] === 'string' ? body['name'] : '',
    email: typeof body['email'] === 'string' ? body['email'] : '',
    role: body['role'],
    type: body['type'] === 'NON_MEMBER' ? 'NON_MEMBER' : 'MEMBER',
  };
}
