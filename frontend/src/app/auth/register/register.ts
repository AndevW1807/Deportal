import { Component, signal } from '@angular/core';
import { email, form, FormField, required, submit, validate } from '@angular/forms/signals';
import { RouterLink } from '@angular/router';
import { AuthLayout } from '../auth-layout/auth-layout';
import { fieldMessage } from '../form/field-message';

@Component({
  imports: [AuthLayout, FormField, RouterLink],
  selector: 'app-register',
  styleUrl: '../form/auth-form.css',
  templateUrl: './register.html',
})
export class Register {
  private readonly model = signal({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
  });

  protected readonly registerForm = form(this.model, (path) => {
    required(path.name, { message: 'Ingresa tu nombre' });
    required(path.email, { message: 'Ingresa tu correo electrónico' });
    email(path.email, {
      message: 'Ingresa un correo válido',
      when: ({ value }) => value().length > 0,
    });
    required(path.password, { message: 'Ingresa tu contraseña' });
    required(path.confirmPassword, { message: 'Confirma tu contraseña' });
    validate(path.confirmPassword, ({ value, valueOf }) => {
      if (value().length === 0 || value() === valueOf(path.password)) {
        return undefined;
      }

      return { kind: 'mismatch', message: 'Las contraseñas no coinciden' };
    });
  });

  protected readonly passwordVisible = signal(false);
  protected readonly confirmVisible = signal(false);
  protected readonly message = fieldMessage;

  protected onSubmit(event: Event): void {
    event.preventDefault();
    void submit(this.registerForm, async () => undefined);
  }
}
