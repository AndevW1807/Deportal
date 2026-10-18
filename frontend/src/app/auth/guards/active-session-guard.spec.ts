import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { authInterceptor } from '../session/auth-interceptor';
import { AuthSession } from '../session/auth-session';
import { activeSessionGuard } from './active-session-guard';

@Component({ template: '' })
class Blank {}

function nextTurn(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 0));
}

describe('activeSessionGuard', () => {
  let http: HttpTestingController;
  let router: Router;
  let session: AuthSession;

  beforeEach(async () => {
    localStorage.clear();
    sessionStorage.clear();

    await TestBed.configureTestingModule({
      providers: [
        provideRouter([
          { path: '', component: Blank, canActivate: [activeSessionGuard] },
          { path: 'login', component: Blank, canActivate: [activeSessionGuard] },
          { path: 'app', component: Blank },
          { path: 'admin/courts', component: Blank },
        ]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    session = TestBed.inject(AuthSession);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
    sessionStorage.clear();
    session.clear();
  });

  it('leaves the login page alone when there is no token', async () => {
    await router.navigateByUrl('/login');

    expect(router.url).toBe('/login');
    http.expectNone('/api/auth/session');
  });

  it('sends an active admin from login to the courts view', async () => {
    session.save('admin-token');
    const pending = router.navigateByUrl('/login');
    await nextTurn();

    http.expectOne('/api/auth/session').flush(sessionUser('ADMIN'));
    await pending;

    expect(router.url).toBe('/admin/courts');
  });

  it('sends an active admin home visit to the courts view', async () => {
    session.save('admin-token');
    const pending = router.navigateByUrl('/');
    await nextTurn();

    http.expectOne('/api/auth/session').flush(sessionUser('ADMIN'));
    await pending;

    expect(router.url).toBe('/admin/courts');
  });

  it('sends an active customer from login to the app', async () => {
    session.save('customer-token');
    const pending = router.navigateByUrl('/login');
    await nextTurn();

    http.expectOne('/api/auth/session').flush(sessionUser('CUSTOMER'));
    await pending;

    expect(router.url).toBe('/app');
  });

  it('sends an active customer home visit to the app', async () => {
    session.save('customer-token');
    const pending = router.navigateByUrl('/');
    await nextTurn();

    http.expectOne('/api/auth/session').flush(sessionUser('CUSTOMER'));
    await pending;

    expect(router.url).toBe('/app');
  });

  it('drops an expired token and stays on login', async () => {
    session.save('expired-token');
    const pending = router.navigateByUrl('/login');
    await nextTurn();

    http.expectOne('/api/auth/session').flush({}, { status: 401, statusText: 'Unauthorized' });
    await pending;

    expect(router.url).toBe('/login');
    expect(session.token()).toBeNull();
  });
});

function sessionUser(role: 'ADMIN' | 'CUSTOMER') {
  return {
    id: 1,
    name: role === 'ADMIN' ? 'Admin' : 'Ana',
    email: role === 'ADMIN' ? 'admin@admin.com' : 'ana@deportal.test',
    role,
    type: 'MEMBER' as const,
  };
}
