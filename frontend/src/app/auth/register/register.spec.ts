import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { Notice } from '../notice/notice';
import { authInterceptor } from '../session/auth-interceptor';
import { Register } from './register';

@Component({ template: '' })
class Blank {}

describe('Register', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;
  let http: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    localStorage.clear();
    sessionStorage.clear();

    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [
        provideRouter([
          { path: '', component: Blank },
          { path: 'login', component: Blank },
          { path: 'register', component: Blank },
        ]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    http = TestBed.inject(HttpTestingController);
    await router.navigateByUrl('/register');

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
    sessionStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('asks for the registration fields', () => {
    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Regístrate');
    expect(text).toContain('Crea tu cuenta para empezar a reservar');
    expect(fixture.nativeElement.querySelector('input[aria-label="Nombre"]')).not.toBeNull();
    expect(
      fixture.nativeElement.querySelector('input[aria-label="Correo electrónico"]'),
    ).not.toBeNull();
    expect(fixture.nativeElement.querySelector('input[aria-label="Contraseña"]')).not.toBeNull();
    expect(
      fixture.nativeElement.querySelector('input[aria-label="Confirmar contraseña"]'),
    ).not.toBeNull();
    expect(text).not.toContain('Recordarme');
    expect(text).not.toContain('¿Olvidaste tu contraseña?');
    expect(text).not.toContain('Google');
    expect(text).not.toContain('Apple');
    expect(text).not.toContain('o continúa con');
    expect(fixture.nativeElement.querySelector('.switch a').textContent).toContain('Inicia sesión');
    expect(fixture.nativeElement.querySelector('button[type="submit"]').textContent).toContain(
      'Registrarse',
    );
  });

  it('reports when the passwords do not match', async () => {
    const name: HTMLInputElement = fixture.nativeElement.querySelector(
      'input[aria-label="Nombre"]',
    );
    const email: HTMLInputElement = fixture.nativeElement.querySelector(
      'input[aria-label="Correo electrónico"]',
    );
    const password: HTMLInputElement = fixture.nativeElement.querySelector(
      'input[aria-label="Contraseña"]',
    );
    const confirm: HTMLInputElement = fixture.nativeElement.querySelector(
      'input[aria-label="Confirmar contraseña"]',
    );

    fill(name, 'Ana');
    fill(email, 'ana@deportal.test');
    fill(password, 'cancha-segura');
    fill(confirm, 'otra-clave');
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Las contraseñas no coinciden');
    expect(TestBed.inject(Notice).text()).toBe('Las contraseñas no coinciden');
    expect(TestBed.inject(Notice).kind()).toBe('error');
    http.expectNone('/api/auth/register');
  });

  it('creates the account and opens the login page', async () => {
    fillAccount('Ana', 'ana@deportal.test', 'cancha-segura');
    submitForm();

    const registration = http.expectOne('/api/auth/register');
    expect(registration.request.body).toEqual({
      name: 'Ana',
      email: 'ana@deportal.test',
      password: 'cancha-segura',
      confirmPassword: 'cancha-segura',
    });
    registration.flush({
      id: 8,
      name: 'Ana',
      email: 'ana@deportal.test',
      role: 'CUSTOMER',
      type: 'NON_MEMBER',
    });
    await nextTurn();
    await fixture.whenStable();

    expect(router.url).toBe('/login');
    expect(TestBed.inject(Notice).text()).toBe('Registro exitoso. Ya puedes iniciar sesión.');
    expect(TestBed.inject(Notice).kind()).toBe('success');
    expect(sessionStorage.getItem('deportal.accessToken')).toBeNull();
    expect(localStorage.getItem('deportal.accessToken')).toBeNull();
    http.expectNone('/api/auth/login');
  });

  it('stays on the form when the email is already registered', async () => {
    fillAccount('Ana', 'ana@deportal.test', 'cancha-segura');
    submitForm();

    http
      .expectOne('/api/auth/register')
      .flush({ detail: 'Email already registered' }, { status: 409, statusText: 'Conflict' });
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Ese correo ya está registrado');
    expect(fixture.nativeElement.textContent).not.toContain('Email already registered');
    expect(TestBed.inject(Notice).text()).toBe('Ese correo ya está registrado');
    expect(TestBed.inject(Notice).kind()).toBe('error');
    expect(router.url).toBe('/register');
    expect(sessionStorage.getItem('deportal.accessToken')).toBeNull();
  });

  it('reports when the server cannot be reached', async () => {
    fillAccount('Ana', 'ana@deportal.test', 'cancha-segura');
    submitForm();

    http.expectOne('/api/auth/register').error(new ProgressEvent('error'));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('No se pudo conectar con el servidor');
    expect(TestBed.inject(Notice).text()).toBe('No se pudo conectar con el servidor');
    expect(TestBed.inject(Notice).kind()).toBe('error');
    expect(router.url).toBe('/register');
  });

  function fillAccount(name: string, email: string, password: string): void {
    fill(fixture.nativeElement.querySelector('input[aria-label="Nombre"]'), name);
    fill(fixture.nativeElement.querySelector('input[aria-label="Correo electrónico"]'), email);
    fill(fixture.nativeElement.querySelector('input[aria-label="Contraseña"]'), password);
    fill(fixture.nativeElement.querySelector('input[aria-label="Confirmar contraseña"]'), password);
  }

  function submitForm(): void {
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
  }
});

function nextTurn(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 0));
}

function fill(input: HTMLInputElement, value: string): void {
  input.value = value;
  input.dispatchEvent(new Event('input'));
}
