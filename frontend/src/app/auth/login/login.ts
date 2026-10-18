import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { email, form, FormField, required, submit } from '@angular/forms/signals';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { viewForRole } from '../active-session-guard';
import { AuthApi } from '../auth-api';
import { AuthSession } from '../auth-session';
import { AuthLayout } from '../auth-layout/auth-layout';
import { fieldMessage } from '../field-message';

@Component({
  imports: [AuthLayout, FormField, RouterLink],
  selector: 'app-login',
  styleUrl: '../auth-form.css',
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthApi);
  private readonly session = inject(AuthSession);
  private readonly router = inject(Router);
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
  protected readonly failure = signal<string | null>(null);
  protected readonly message = fieldMessage;

  protected onSubmit(event: Event): void {
    event.preventDefault();
    this.failure.set(null);
    void submit(this.loginForm, async () => {
      const { email: address, password, remember } = this.model();
      try {
        const token = await firstValueFrom(this.auth.login(address, password));
        this.session.save(token.accessToken, remember);
        const user = await firstValueFrom(this.auth.session());
        await this.router.navigateByUrl(viewForRole(user.role));
      } catch (error) {
        this.session.clear();
        this.failure.set(failureMessage(error));
      }
    });
  }
}

function failureMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse && error.status === HttpStatusCode.Unauthorized) {
    return 'Correo o contraseña incorrectos';
  }

  return 'No se pudo conectar con el servidor';
}
