import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { authInterceptor } from './auth-interceptor';
import { AuthSession } from './auth-session';

@Component({ template: '' })
class Blank {}

describe('authInterceptor', () => {
  let http: HttpTestingController;
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
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    session = TestBed.inject(AuthSession);
    await router.navigateByUrl('/login');
  });

  afterEach(() => {
    http.verify();
    session.clear();
    sessionStorage.clear();
  });

  it('sends a protected page to login when the api rejects the token', async () => {
    session.save('customer-token');
    await router.navigateByUrl('/app/reserve');

    TestBed.inject(HttpClient)
      .get('/api/reservations')
      .subscribe({ error: () => undefined });
    http.expectOne('/api/reservations').flush({}, { status: 401, statusText: 'Unauthorized' });
    await new Promise((resolve) => setTimeout(resolve, 0));

    expect(router.url).toBe('/login');
    expect(session.token()).toBeNull();
  });

  it('leaves a failed login on the login page', async () => {
    TestBed.inject(HttpClient)
      .post('/api/auth/login', {})
      .subscribe({ error: () => undefined });
    http.expectOne('/api/auth/login').flush({}, { status: 401, statusText: 'Unauthorized' });
    await new Promise((resolve) => setTimeout(resolve, 0));

    expect(router.url).toBe('/login');
  });
});
