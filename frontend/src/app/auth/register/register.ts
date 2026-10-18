import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { email, form, FormField, required, submit, validate } from '@angular/forms/signals';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthLayout } from '../auth-layout/auth-layout';
import { fieldMessage } from '../form/field-message';
import { Notice } from '../notice/notice';
import { AuthService } from '../session/auth-service';

@Component({
  imports: [AuthLayout, FormField, RouterLink],
  selector: 'app-register',
  styleUrl: '../form/auth-form.css',
  templateUrl: './register.html',
})
export class Register {
  private readonly auth = inject(AuthService);
  private readonly notice = inject(Notice);
  private readonly router = inject(Router);
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
  protected readonly failure = signal<string | null>(null);
  protected readonly message = fieldMessage;

  protected onSubmit(event: Event): void {
    event.preventDefault();
    this.failure.set(null);
    void submit(this.registerForm, async () => {
      const { name, email: address, password, confirmPassword } = this.model();
      try {
        await firstValueFrom(this.auth.register(name, address, password, confirmPassword));
        this.notice.success('Registro exitoso. Ya puedes iniciar sesión.');
        await this.router.navigateByUrl('/login');
      } catch (error) {
        this.failure.set(failureMessage(error));
      }
    });
  }
}

function failureMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'No se pudo conectar con el servidor';
  }
  if (error.status === HttpStatusCode.Conflict) {
    return 'Ese correo ya está registrado';
  }
  if (error.status === HttpStatusCode.BadRequest) {
    return 'Revisa los datos e intenta de nuevo';
  }

  return 'No se pudo conectar con el servidor';
}
