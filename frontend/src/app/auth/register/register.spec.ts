import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Register } from './register';

describe('Register', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    await fixture.whenStable();
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
  });
});

function fill(input: HTMLInputElement, value: string): void {
  input.value = value;
  input.dispatchEvent(new Event('input'));
}
