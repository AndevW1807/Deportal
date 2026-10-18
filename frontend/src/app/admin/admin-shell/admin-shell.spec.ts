import { BreakpointObserver } from '@angular/cdk/layout';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { authInterceptor } from '../../auth/session/auth-interceptor';
import { AdminShell } from './admin-shell';

@Component({ template: '' })
class Blank {}

describe('AdminShell', () => {
  let fixture: ComponentFixture<AdminShell>;
  let http: HttpTestingController;

  async function setup(narrow: boolean, role: 'ADMIN' | 'CUSTOMER' = 'ADMIN'): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [AdminShell],
      providers: [
        provideRouter([
          { path: 'app', component: Blank },
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
    fixture = TestBed.createComponent(AdminShell);
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
    expect(profile.tagName).toBe('P');
    expect(profile.textContent).toContain('Admin');
    expect(fixture.nativeElement.querySelector('button.profile')).toBeNull();
    expect(fixture.nativeElement.querySelector('[aria-label="Abrir menú"]')).toBeNull();
  });

  it('hides Canchas from a customer', async () => {
    await setup(false, 'CUSTOMER');
    const brand: HTMLAnchorElement = fixture.nativeElement.querySelector('a.brand');

    expect(fixture.nativeElement.querySelector('a[mat-list-item]')).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Canchas');
    expect(fixture.nativeElement.textContent).not.toContain('Usuarios');
    expect(brand.getAttribute('href')).toBe('/app');
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
