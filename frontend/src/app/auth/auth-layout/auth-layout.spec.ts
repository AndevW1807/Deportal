import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthLayout } from './auth-layout';

describe('AuthLayout', () => {
  let component: AuthLayout;
  let fixture: ComponentFixture<AuthLayout>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthLayout],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(AuthLayout);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows the brand and a way back home', () => {
    const text = fixture.nativeElement.textContent;
    const back: HTMLAnchorElement = fixture.nativeElement.querySelector('a.back');

    expect(text).toContain('Reserva, juega y vive el deporte');
    expect(text).toContain('en un solo lugar.');
    expect(back.textContent).toContain('Volver al inicio');
    expect(back.getAttribute('href')).toBe('/');
    expect(fixture.nativeElement.querySelector('a.home').textContent).toContain('Inicio');
  });
});
