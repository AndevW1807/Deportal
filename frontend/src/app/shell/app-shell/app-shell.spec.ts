import { BreakpointObserver } from '@angular/cdk/layout';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthSession } from '../../auth/session/auth-session';
import { of } from 'rxjs';
import { authInterceptor } from '../../auth/session/auth-interceptor';
import { AppShell } from './app-shell';

@Component({ template: '' })
class Blank {}

describe('AppShell', () => {
  let fixture: ComponentFixture<AppShell>;
  let http: HttpTestingController;

  async function setup(narrow: boolean, role: 'ADMIN' | 'CUSTOMER' = 'ADMIN'): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [AppShell],
      providers: [
        provideRouter([
          { path: 'app', component: Blank },
          { path: 'app/reserve', component: Blank },
          { path: 'app/reservations', component: Blank },
          { path: 'login', component: Blank },
          { path: 'admin/courts', component: Blank },
        ]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        {
          provide: BreakpointObserver,
          useValue: { observe: () => of({ matches: narrow, breakpoints: {} }) },
        },
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(AppShell);
    http.expectOne('/api/auth/session').flush({
      id: 1,
      name: role === 'ADMIN' ? 'Admin' : 'Ana',
      email: role === 'ADMIN' ? 'admin@admin.com' : 'ana@deportal.test',
      role,
      type: 'MEMBER',
    });
    await fixture.whenStable();
  }

  afterEach(() => {
    http.verify();
  });

  it('shows Canchas and the signed-in name', async () => {
    await setup(false);
    const links: NodeListOf<HTMLAnchorElement> =
      fixture.nativeElement.querySelectorAll('a[mat-list-item]');
    const profile: HTMLElement = fixture.nativeElement.querySelector('.profile');

    expect(links[0].textContent).toContain('Canchas');
    expect(links[0].getAttribute('href')).toBe('/admin/courts');
    expect(links[1].textContent).toContain('Usuarios');
    expect(links[1].getAttribute('href')).toBe('/admin/users');
    expect(links[2].textContent).toContain('Reportes');
    expect(links[2].getAttribute('href')).toBe('/admin/reports');
    expect(profile.tagName).toBe('P');
    expect(profile.textContent).toContain('Admin');
    expect(fixture.nativeElement.querySelector('button.profile')).toBeNull();
    expect(fixture.nativeElement.querySelector('[aria-label="Abrir menú"]')).toBeNull();
    expect(fixture.nativeElement.querySelector('.logout').textContent).toContain('Cerrar sesión');
    expect(fixture.nativeElement.textContent).not.toContain('Reservar cancha');
    expect(fixture.nativeElement.textContent).not.toContain('Mis reservas');
  });

  it('shows Reservar cancha to a customer', async () => {
    await setup(false, 'CUSTOMER');
    const links: NodeListOf<HTMLAnchorElement> =
      fixture.nativeElement.querySelectorAll('a[mat-list-item]');
    const brand: HTMLAnchorElement = fixture.nativeElement.querySelector('a.brand');

    expect(links).toHaveLength(2);
    expect(links[0].textContent).toContain('Reservar cancha');
    expect(links[0].getAttribute('href')).toBe('/app/reserve');
    expect(links[1].textContent).toContain('Mis reservas');
    expect(links[1].getAttribute('href')).toBe('/app/reservations');
    expect(fixture.nativeElement.textContent).not.toContain('Canchas');
    expect(fixture.nativeElement.textContent).not.toContain('Usuarios');
    expect(fixture.nativeElement.textContent).not.toContain('Reportes');
    expect(brand.getAttribute('href')).toBe('/app/reserve');
    expect(fixture.nativeElement.querySelector('.logout').textContent).toContain('Cerrar sesión');
  });

  it('signs out and returns to login', async () => {
    await setup(false);
    const session = TestBed.inject(AuthSession);
    session.save('token');

    fixture.nativeElement.querySelector('.logout').click();
    await fixture.whenStable();

    expect(session.token()).toBeNull();
    expect(TestBed.inject(Router).url).toBe('/login');
  });

  it('opens the menu from the toolbar and closes it from the backdrop', async () => {
    await setup(true);
    const open: HTMLButtonElement = fixture.nativeElement.querySelector(
      '[aria-label="Abrir menú"]',
    );

    open.click();
    await fixture.whenStable();

    expect(open.getAttribute('aria-expanded')).toBe('true');

    fixture.nativeElement.querySelector('.mat-drawer-backdrop').click();
    await fixture.whenStable();

    expect(open.getAttribute('aria-expanded')).toBe('false');
  });
});
