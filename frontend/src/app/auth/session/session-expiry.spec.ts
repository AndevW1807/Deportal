import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthSession } from './auth-session';
import { SessionExpiry } from './session-expiry';

@Component({ template: '' })
class Blank {}

function tokenExpiringAt(epochMs: number): string {
  const payload = btoa(JSON.stringify({ exp: Math.floor(epochMs / 1000) }))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/g, '');
  return `eyJhbGciOiJub25lIn0.${payload}.sig`;
}

describe('SessionExpiry', () => {
  let router: Router;
  let session: AuthSession;

  beforeEach(async () => {
    sessionStorage.clear();

    await TestBed.configureTestingModule({
      providers: [
        provideRouter([
          { path: 'login', component: Blank },
          { path: 'app/reserve', component: Blank },
        ]),
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    session = TestBed.inject(AuthSession);
    await router.navigateByUrl('/login');
  });

  afterEach(() => {
    session.clear();
    sessionStorage.clear();
  });

  it('leaves a protected page when the stored token reaches its expiry', async () => {
    await router.navigateByUrl('/app/reserve');
    const expSeconds = Math.floor(Date.now() / 1000) + 1;
    session.save(tokenExpiringAt(expSeconds * 1000));
    TestBed.inject(SessionExpiry);
    TestBed.flushEffects();

    expect(router.url).toBe('/app/reserve');
    await new Promise((resolve) => setTimeout(resolve, expSeconds * 1000 - Date.now() + 30));

    expect(router.url).toBe('/login');
    expect(session.token()).toBeNull();
  });

  it('drops an already expired token without leaving a public page', async () => {
    session.save(tokenExpiringAt(Date.now() - 1000));
    TestBed.inject(SessionExpiry);
    TestBed.flushEffects();
    await new Promise((resolve) => setTimeout(resolve, 0));

    expect(router.url).toBe('/login');
    expect(session.token()).toBeNull();
  });
});
