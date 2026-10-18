import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { email, form, FormField, required, submit } from '@angular/forms/signals';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthLayout } from '../auth-layout/auth-layout';
import { fieldMessage, fieldMessages } from '../form/field-message';
import { viewForRole } from '../guards/active-session-guard';
import { Notice } from '../notice/notice';
import { AuthService } from '../session/auth-service';
import { AuthSession } from '../session/auth-session';

@Component({
  imports: [AuthLayout, FormField, RouterLink],
  selector: 'app-login',
  styleUrl: '../form/auth-form.css',
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly notice = inject(Notice);
  private readonly session = inject(AuthSession);
  private readonly router = inject(Router);
  private readonly model = signal({
    email: '',
    password: '',
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
    this.notice.clear();
    void submit(this.loginForm, async () => {
      const { email: address, password } = this.model();
      try {
        const token = await firstValueFrom(this.auth.login(address, password));
        this.session.save(token.accessToken);
        const user = await firstValueFrom(this.auth.session());
        await this.router.navigateByUrl(viewForRole(user.role));
      } catch (error) {
        this.session.clear();
        const text = failureMessage(error);
        this.failure.set(text);
        this.notice.error(text);
      }
    }).then((accepted) => {
      if (accepted) {
        return;
      }

      const text = fieldMessages([this.loginForm.email(), this.loginForm.password()]);
      if (text) {
        this.notice.error(text);
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
