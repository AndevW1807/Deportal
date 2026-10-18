import { HttpErrorResponse, HttpStatusCode } from '@angular/common/http';
import { Component, effect, inject, signal } from '@angular/core';
import { form, FormField, max, maxLength, min, required, submit } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { fieldMessage, fieldMessages } from '../../auth/form/field-message';
import { Notice } from '../../auth/notice/notice';
import { CourtService } from './court-service';
import { Court, SportType } from './court.model';

export interface CourtEditorData {
  court: Court;
  sportTypes: SportType[];
  onSaved: (court: Court) => void;
}

const WINDOW_MESSAGE = 'El horario debe estar entre 06:00 y 22:00';
const ORDER_MESSAGE = 'La apertura debe ser antes del cierre';

@Component({
  selector: 'app-court-editor',
  imports: [FormField],
  templateUrl: './court-editor.html',
  styleUrl: './court-editor.css',
})
export class CourtEditor {
  protected readonly data = inject<CourtEditorData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<CourtEditor, Court | undefined>);
  private readonly courts = inject(CourtService);
  private readonly notice = inject(Notice);
  private readonly model = signal({
    name: this.data.court.name,
    sportTypeCode: this.data.court.sportType.code,
    capacity: this.data.court.capacity,
    opensAt: this.data.court.opensAt,
    closesAt: this.data.court.closesAt,
    hourlyRate: Number(this.data.court.hourlyRate),
  });

  protected readonly editorForm = form(this.model, (path) => {
    required(path.name, { message: 'Ingresa el nombre' });
    maxLength(path.name, 120, { message: 'Ingresa el nombre' });
    required(path.sportTypeCode, { message: 'Elige un deporte' });
    required(path.capacity, { message: 'La capacidad debe estar entre 1 y 50' });
    min(path.capacity, 1, { message: 'La capacidad debe estar entre 1 y 50' });
    max(path.capacity, 50, { message: 'La capacidad debe estar entre 1 y 50' });
    required(path.opensAt, { message: WINDOW_MESSAGE });
    required(path.closesAt, { message: WINDOW_MESSAGE });
    required(path.hourlyRate, { message: 'La tarifa mínima es $5.00' });
    min(path.hourlyRate, 5, { message: 'La tarifa mínima es $5.00' });
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

  protected onSubmit(event: Event): void {
    event.preventDefault();
    this.nameTaken.set(false);
    void submit(this.editorForm, async () => {
      if (this.hoursMessage()) {
        this.notice.error(this.hoursMessage() ?? WINDOW_MESSAGE);
        return;
      }

      const draft = this.model();
      this.submitting.set(true);
      try {
        const updated = await firstValueFrom(
          this.courts.update(this.data.court.id, {
            name: draft.name.trim(),
            sportTypeCode: draft.sportTypeCode,
            capacity: draft.capacity,
            opensAt: draft.opensAt,
            closesAt: draft.closesAt,
            hourlyRate: draft.hourlyRate.toFixed(2),
          }),
        );
        this.data.onSaved(updated);
        this.dialogRef.close(updated);
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

function inWindow(value: string): boolean {
  return value >= '06:00' && value <= '22:00';
}
