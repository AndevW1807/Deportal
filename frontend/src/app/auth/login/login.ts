import { Component, signal } from '@angular/core';
import { email, form, FormField, required, submit } from '@angular/forms/signals';
import { RouterLink } from '@angular/router';
import { AuthLayout } from '../auth-layout/auth-layout';
import { fieldMessage } from '../field-message';

@Component({
  imports: [AuthLayout, FormField, RouterLink],
  selector: 'app-login',
  styleUrl: '../auth-form.css',
  templateUrl: './login.html',
})
export class Login {
  private readonly model = signal({
    email: '',
    password: '',
    remember: false,
  });

  protected readonly loginForm = form(this.model, (path) => {
    required(path.email, { message: 'Ingresa tu correo electrónico' });
    email(path.email, {
      message: 'Ingresa un correo válido',
      when: ({ value }) => value().length > 0,
    });
    required(path.password, { message: 'Ingresa tu contraseña' });
  });

  protected readonly passwordVisible = signal(false);
  protected readonly message = fieldMessage;

  protected onSubmit(event: Event): void {
    event.preventDefault();
    void submit(this.loginForm, async () => undefined);
  }
}
