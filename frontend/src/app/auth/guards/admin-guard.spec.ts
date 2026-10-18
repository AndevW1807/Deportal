import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { Courts } from '../../admin/courts/courts';
import { authInterceptor } from '../session/auth-interceptor';
import { AuthSession } from '../session/auth-session';
import { adminGuard } from './admin-guard';

@Component({ template: '' })
class Blank {}

function nextTurn(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 0));
}

describe('adminGuard', () => {
  let http: HttpTestingController;
  let router: Router;
  let session: AuthSession;

  beforeEach(async () => {
    localStorage.clear();
    sessionStorage.clear();

    await TestBed.configureTestingModule({
      providers: [
        provideRouter([
          { path: '', component: Blank },
          { path: 'login', component: Blank },
          { path: 'admin/courts', component: Courts, canActivate: [adminGuard] },
        ]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    session = TestBed.inject(AuthSession);
    await router.navigateByUrl('/');
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
    sessionStorage.clear();
    session.clear();
  });

  it('sends a visitor to the login page', async () => {
    await router.navigateByUrl('/admin/courts');

    expect(router.url).toBe('/login');
    http.expectNone('/api/auth/session');
  });

  it('lets an admin open the courts view', async () => {
    session.save('admin-token', false);
    expect(session.token()).toBe('admin-token');
    const pending = router.navigateByUrl('/admin/courts');
    await new Promise((resolve) => setTimeout(resolve, 0));

    const request = http.expectOne('/api/auth/session');
    expect(request.request.headers.get('Authorization')).toBe('Bearer admin-token');
    request.flush({
      id: 1,
      name: 'Admin',
      email: 'admin@admin.com',
      role: 'ADMIN',
      type: 'MEMBER',
    });
    await pending;

    expect(router.url).toBe('/admin/courts');
  });

  it('sends a customer back home', async () => {
    session.save('customer-token', false);
    const pending = router.navigateByUrl('/admin/courts');
    await nextTurn();

    http.expectOne('/api/auth/session').flush({
      id: 2,
      name: 'Ana',
      email: 'ana@deportal.test',
      role: 'CUSTOMER',
      type: 'MEMBER',
    });
    await pending;

    expect(router.url).toBe('/');
    expect(session.token()).toBe('customer-token');
  });

  it('clears an expired session and returns to login', async () => {
    session.save('expired-token', false);
    const pending = router.navigateByUrl('/admin/courts');
    await nextTurn();

    http.expectOne('/api/auth/session').flush({}, { status: 401, statusText: 'Unauthorized' });
    await pending;

    expect(router.url).toBe('/login');
    expect(session.token()).toBeNull();
  });
});
