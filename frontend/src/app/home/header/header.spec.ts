import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Header } from './header';

describe('Header', () => {
  let component: Header;
  let fixture: ComponentFixture<Header>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Header],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Header);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows the logo and the session buttons', () => {
    const image: HTMLImageElement = fixture.nativeElement.querySelector('img');
    const login: HTMLAnchorElement = fixture.nativeElement.querySelector('a[href="/login"]');
    const register: HTMLAnchorElement = fixture.nativeElement.querySelector('a[href="/register"]');

    expect(image.alt).toBe('Deportal');
    expect(image.getAttribute('src')).toContain('logo.webp');
    expect(login.textContent).toContain('Iniciar sesión');
    expect(register.textContent).toContain('Registrarse');
    expect(fixture.nativeElement.querySelector('a.home')).toBeNull();
    expect(fixture.nativeElement.querySelector('a.home-icon')).toBeNull();
  });

  it('shows Inicio only when the screen asks for it', async () => {
    fixture.componentRef.setInput('showHomeLink', true);
    await fixture.whenStable();

    const home: HTMLAnchorElement = fixture.nativeElement.querySelector('a.home');
    const icon: HTMLAnchorElement = fixture.nativeElement.querySelector('a.home-icon');

    expect(home.textContent).toContain('Inicio');
    expect(home.getAttribute('href')).toBe('/');
    expect(icon.getAttribute('aria-label')).toBe('Volver al inicio');
    expect(icon.getAttribute('href')).toBe('/');
  });
});
