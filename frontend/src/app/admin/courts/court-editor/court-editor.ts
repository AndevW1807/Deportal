import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { form, FormField, max, maxLength, min, required, submit } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIcon } from '@angular/material/icon';
import { firstValueFrom } from 'rxjs';
import { fieldMessage, fieldMessages } from '../../../shared/form/field-message';
import { Notice } from '../../../shared/notice/notice';
import { Court, sportLook } from '../../../shared/court/court.model';
import { formatCop } from '../../../shared/format/money';
import { CourtService } from '../court-service';
import {
  CAPACITY_MESSAGE,
  CourtEditorData,
  MAX_HOURLY_RATE,
  ORDER_MESSAGE,
  RATE_MESSAGE,
  WINDOW_MESSAGE,
  acceptsCapacity,
  acceptsRate,
  formModel,
  inWindow,
  restrict,
} from './court-editor.form';

export type { CourtEditorData };

@Component({
  selector: 'app-court-editor',
  imports: [FormField, MatIcon],
  templateUrl: './court-editor.html',
  styleUrl: './court-editor.css',
})
export class CourtEditor {
  protected readonly data = inject<CourtEditorData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<CourtEditor, Court | undefined>);
  private readonly courts = inject(CourtService);
  private readonly notice = inject(Notice);
  protected readonly title = this.data.court ? 'Editar cancha' : 'Agregar cancha';
  protected readonly hint = this.data.court
    ? 'Actualiza los datos de la cancha.'
    : 'Completa los datos de la nueva cancha.';
  private readonly model = signal(formModel(this.data));
  protected readonly formattedRate = computed(() => {
    const amount = this.model().hourlyRate;
    return typeof amount === 'number' && Number.isFinite(amount) ? formatCop(amount) : '';
  });
  protected readonly sportIcon = computed(() => {
    const code = this.model().sportTypeCode;
    const sport = this.data.sportTypes.find((item) => item.code === code);
    return sport ? sportLook(sport).icon : 'stadium';
  });

  protected readonly editorForm = form(this.model, (path) => {
    required(path.name, { message: 'Ingresa el nombre' });
    maxLength(path.name, 120, { message: 'Ingresa el nombre' });
    required(path.sportTypeCode, { message: 'Elige un deporte' });
    required(path.capacity, { message: CAPACITY_MESSAGE });
    min(path.capacity, 1, { message: CAPACITY_MESSAGE });
    max(path.capacity, 50, { message: CAPACITY_MESSAGE });
    required(path.opensAt, { message: WINDOW_MESSAGE });
    required(path.closesAt, { message: WINDOW_MESSAGE });
    required(path.hourlyRate, { message: RATE_MESSAGE });
    min(path.hourlyRate, 5, { message: RATE_MESSAGE });
    max(path.hourlyRate, MAX_HOURLY_RATE, { message: `La tarifa máxima es ${formatCop(MAX_HOURLY_RATE)}` });
  });

  protected readonly submitting = signal(false);
  protected readonly nameTaken = signal(false);
  protected readonly message = fieldMessage;

  constructor() {
    let seenName = this.model().name;
    effect(() => {
      const name = this.model().name;
      if (name === seenName) {
        return;
      }

      seenName = name;
      this.nameTaken.set(false);
    });
  }

  protected hoursMessage(): string | null {
    const opens = this.editorForm.opensAt();
    const closes = this.editorForm.closesAt();
    const opensError = this.message(opens);
    const closesError = this.message(closes);
    if (opensError) {
      return opensError;
    }
    if (closesError) {
      return closesError;
    }

    const opensAt = this.model().opensAt;
    const closesAt = this.model().closesAt;
    if (opensAt.length === 0 || closesAt.length === 0) {
      return null;
    }
    if (!inWindow(opensAt) || !inWindow(closesAt)) {
      return WINDOW_MESSAGE;
    }
    if (opensAt >= closesAt) {
      return ORDER_MESSAGE;
    }

    return null;
  }

  protected cancel(): void {
    this.dialogRef.close();
  }

  protected restrictCapacity(event: InputEvent): void {
    restrict(event, acceptsCapacity);
  }

  protected restrictRate(event: InputEvent): void {
    restrict(event, acceptsRate);
  }

  protected onSubmit(event: Event): void {
    event.preventDefault();
    this.nameTaken.set(false);
    void submit(this.editorForm, async () => {
      if (this.hoursMessage()) {
        this.notice.error(this.hoursMessage() ?? WINDOW_MESSAGE);
        return;
      }

      const draft = this.model();
      const body = {
        name: draft.name.trim(),
        sportTypeCode: draft.sportTypeCode,
        capacity: draft.capacity ?? 0,
        opensAt: draft.opensAt,
        closesAt: draft.closesAt,
        hourlyRate: draft.hourlyRate.toFixed(2),
      };
      this.submitting.set(true);
      try {
        const saved = await firstValueFrom(
          this.data.court
            ? this.courts.update(this.data.court.id, body)
            : this.courts.create(body),
        );
        this.data.onSaved(saved);
        this.dialogRef.close(saved);
      } catch (error) {
        if (error instanceof HttpErrorResponse && error.status === HttpStatusCode.Conflict) {
          this.nameTaken.set(true);
          this.notice.error('Ya existe una cancha con ese nombre');
          return;
        }

        const text =
          error instanceof HttpErrorResponse && error.status === HttpStatusCode.BadRequest
            ? 'Revisa los datos e intenta de nuevo'
            : 'No se pudo conectar con el servidor';
        this.notice.error(text);
      } finally {
        this.submitting.set(false);
      }
    }).then((accepted) => {
      if (accepted) {
        return;
      }

      const text =
        fieldMessages([
          this.editorForm.name(),
          this.editorForm.sportTypeCode(),
          this.editorForm.capacity(),
          this.editorForm.opensAt(),
          this.editorForm.closesAt(),
          this.editorForm.hourlyRate(),
        ]) ?? this.hoursMessage();
      if (text) {
        this.notice.error(text);
      }
    });
  }
}
