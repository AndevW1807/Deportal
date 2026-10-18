import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Login } from './login';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows the sign-in form without calling an API', () => {
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
  });
});
