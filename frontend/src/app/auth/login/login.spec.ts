import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { authInterceptor } from '../session/auth-interceptor';
import { Login } from './login';

@Component({ template: '' })
class Blank {}

describe('Login', () => {
  let fixture: ComponentFixture<Login>;
  let http: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    localStorage.clear();
    sessionStorage.clear();

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideRouter([
          { path: '', component: Blank },
          { path: 'login', component: Blank },
          { path: 'admin/courts', component: Blank },
        ]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    http = TestBed.inject(HttpTestingController);
    await router.navigateByUrl('/login');

    fixture = TestBed.createComponent(Login);
    await fixture.whenStable();
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
    sessionStorage.clear();
  });

  it('shows the sign-in form', () => {
    const text = fixture.nativeElement.textContent;
    const form: HTMLFormElement = fixture.nativeElement.querySelector('form');
    const submit: HTMLButtonElement = fixture.nativeElement.querySelector('button[type="submit"]');

    expect(text).toContain('Inicia sesión');
    expect(text).toContain('Ingresa a tu cuenta para continuar');
    expect(text).toContain('Recordarme');
    expect(text).toContain('¿Olvidaste tu contraseña?');
    expect(text).toContain('Google');
    expect(text).toContain('Apple');
    expect(fixture.nativeElement.querySelector('.switch a').textContent).toContain('Crear cuenta');
    expect(form.getAttribute('action')).toBeNull();
    expect(submit.textContent).toContain('Iniciar sesión');
    expect(fixture.nativeElement.querySelectorAll('button.provider').length).toBe(2);
    http.expectNone('/api/auth/login');
  });

  it('stays on the login page when the credentials are rejected', async () => {
    fillCredentials('admin@admin.com', 'incorrecta');
    submitForm();

    const request = http.expectOne('/api/auth/login');
    expect(request.request.body).toEqual({ email: 'admin@admin.com', password: 'incorrecta' });
    request.flush({ detail: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Correo o contraseña incorrectos');
    expect(router.url).toBe('/login');
    expect(sessionStorage.getItem('deportal.accessToken')).toBeNull();
  });

  it('sends an admin to the courts view and remembers the token', async () => {
    fillCredentials('admin@admin.com', '123456');
    const remember: HTMLInputElement = fixture.nativeElement.querySelector(
      'input[type="checkbox"]',
    );
    remember.checked = true;
    remember.dispatchEvent(new Event('input'));
    submitForm();

    http.expectOne('/api/auth/login').flush({
      tokenType: 'Bearer',
      accessToken: 'admin-token',
      expiresIn: 900,
    });
    await nextTurn();
    const session = http.expectOne('/api/auth/session');
    expect(session.request.headers.get('Authorization')).toBe('Bearer admin-token');
    session.flush(sessionUser('ADMIN'));
    await nextTurn();
    await fixture.whenStable();

    expect(router.url).toBe('/admin/courts');
    expect(localStorage.getItem('deportal.accessToken')).toBe('admin-token');
    expect(sessionStorage.getItem('deportal.accessToken')).toBeNull();
  });

  it('sends a customer back home for this browser session', async () => {
    fillCredentials('ana@deportal.test', '123456');
    submitForm();

    http.expectOne('/api/auth/login').flush({
      tokenType: 'Bearer',
      accessToken: 'customer-token',
      expiresIn: 900,
    });
    await nextTurn();
    http.expectOne('/api/auth/session').flush(sessionUser('CUSTOMER'));
    await nextTurn();
    await fixture.whenStable();

    expect(router.url).toBe('/');
    expect(sessionStorage.getItem('deportal.accessToken')).toBe('customer-token');
    expect(localStorage.getItem('deportal.accessToken')).toBeNull();
  });

  it('reports when the server cannot be reached', async () => {
    fillCredentials('admin@admin.com', '123456');
    submitForm();

    http.expectOne('/api/auth/login').error(new ProgressEvent('error'));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('No se pudo conectar con el servidor');
    expect(router.url).toBe('/login');
  });

  function fillCredentials(email: string, password: string): void {
    fill(fixture.nativeElement.querySelector('input[aria-label="Correo electrónico"]'), email);
    fill(fixture.nativeElement.querySelector('input[aria-label="Contraseña"]'), password);
  }

  function submitForm(): void {
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
  }
});

function fill(input: HTMLInputElement, value: string): void {
  input.value = value;
  input.dispatchEvent(new Event('input'));
}

function nextTurn(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 0));
}

function sessionUser(role: 'ADMIN' | 'CUSTOMER') {
  return {
    id: 1,
    name: role === 'ADMIN' ? 'Admin' : 'Ana',
    email: role === 'ADMIN' ? 'admin@admin.com' : 'ana@deportal.test',
    role,
    type: 'MEMBER',
  };
}
