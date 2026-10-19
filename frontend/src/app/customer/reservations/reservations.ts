import { httpResource } from '@angular/common/http';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { formatCop } from '../../shared/format/money';
import { httpMessage } from '../../shared/http/http-message';
import { Notice } from '../../shared/notice/notice';
import { ReservationService } from '../shared/reservation-service';
import {
  MyReservation,
  formatDay,
  parseReservations,
  reservationStatusLabel,
} from '../shared/reservation.model';

@Component({
  selector: 'app-my-reservations',
  templateUrl: './reservations.html',
  styleUrls: ['../../shared/page/page.css', './reservations.css'],
})
export class MyReservations {
  private readonly reservations = inject(ReservationService);
  private readonly notice = inject(Notice);
  protected readonly busy = signal(false);
  protected readonly formatDay = formatDay;
  protected readonly statusLabel = reservationStatusLabel;
  protected readonly money = (amount: string | null): string => formatCop(Number(amount ?? '0'));
  private readonly resource = httpResource(() => '/api/reservations', {
    defaultValue: [] as MyReservation[],
    parse: parseReservations,
  });

  protected readonly rows = computed(() => (this.resource.hasValue() ? this.resource.value() : []));

  constructor() {
    effect(() => {
      if (this.resource.status() === 'error') {
        this.notice.error('No se pudo conectar con el servidor');
      }
    });
  }

  protected hoursLabel(hours: number): string {
    return hours === 1 ? '1 hora' : `${hours} horas`;
  }

  protected async cancel(row: MyReservation): Promise<void> {
    if (!row.cancellable || this.busy()) {
      return;
    }

    this.busy.set(true);
    try {
      const cancelled = await firstValueFrom(this.reservations.cancel(row.id));
      const refund = formatCop(Number(cancelled.refundAmount ?? '0'));
      this.notice.success(`Reserva cancelada. Reembolso: ${refund}`);
      this.resource.reload();
    } catch (error: unknown) {
      this.notice.error(messageOf(error));
    } finally {
      this.busy.set(false);
    }
  }
}

function messageOf(error: unknown): string {
  return httpMessage(error, {
    400: 'Esa reserva ya no se puede cancelar.',
    404: 'No se encontró la reserva.',
  });
}
