import { httpResource } from '@angular/common/http';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { Court } from '../../shared/court/court.model';
import { parseCourts } from '../../shared/court/parse-court';
import { httpMessage } from '../../shared/http/http-message';
import { Notice } from '../../shared/notice/notice';
import { ReservationService } from '../shared/reservation-service';
import {
  Slot,
  bogotaToday,
  monthCells,
  monthTitle,
  parseQuote,
  parseSlots,
  shiftMonth,
} from '../shared/reservation.model';
import { ReserveCourtStep } from './reserve-court-step';
import { ReserveDateStep } from './reserve-date-step';
import { ReserveHoursStep } from './reserve-hours-step';
import { ReserveSummary } from './reserve-summary';

@Component({
  selector: 'app-reserve',
  imports: [ReserveCourtStep, ReserveDateStep, ReserveHoursStep, ReserveSummary],
  templateUrl: './reserve.html',
  styleUrls: ['../../shared/page/page.css', './reserve.css'],
})
export class Reserve {
  private readonly reservations = inject(ReservationService);
  private readonly notice = inject(Notice);
  protected readonly today = bogotaToday();
  protected readonly steps = ['Cancha', 'Horario', 'Resumen'] as const;
  protected readonly step = signal(0);
  protected readonly query = signal('');
  protected readonly sport = signal('');
  protected readonly courtId = signal<number | null>(null);
  protected readonly date = signal(this.today);
  protected readonly duration = signal(1);
  protected readonly start = signal<string | null>(null);
  protected readonly month = signal(monthOf(this.today));
  protected readonly busy = signal(false);

  protected readonly courtsResource = httpResource(() => '/api/reservations/courts', {
    defaultValue: [] as Court[],
    parse: parseCourts,
  });
  protected readonly availability = httpResource(() => {
    const courtId = this.courtId();
    if (courtId === null) {
      return undefined;
    }

    const params = new URLSearchParams({
      courtId: String(courtId),
      date: this.date(),
      durationHours: String(this.duration()),
    });
    return `/api/reservations/availability?${params}`;
  }, {
    defaultValue: [] as Slot[],
    parse: parseSlots,
  });
  protected readonly quoteResource = httpResource(() => {
    const courtId = this.courtId();
    const start = this.start();
    if (courtId === null || start === null) {
      return undefined;
    }

    const params = new URLSearchParams({
      courtId: String(courtId),
      startsAt: start,
      durationHours: String(this.duration()),
    });
    return `/api/reservations/quote?${params}`;
  }, {
    parse: parseQuote,
  });

  protected readonly courts = computed(() =>
    this.courtsResource.hasValue() ? this.courtsResource.value() : [],
  );
  protected readonly sports = computed(() => {
    const seen = new Map<string, string>();
    for (const court of this.courts()) {
      seen.set(court.sportType.code, court.sportType.name);
    }

    return [...seen.entries()].map(([code, name]) => ({ code, name }));
  });
  protected readonly visibleCourts = computed(() => {
    const text = this.query().trim().toLocaleLowerCase('es');
    const sport = this.sport();
    return this.courts().filter((court) => {
      const matchesSport = sport.length === 0 || court.sportType.code === sport;
      const matchesName = court.name.toLocaleLowerCase('es').includes(text);
      return matchesSport && matchesName;
    });
  });
  protected readonly court = computed(
    () => this.courts().find((item) => item.id === this.courtId()) ?? null,
  );
  protected readonly monthLabel = computed(() => monthTitle(this.month().year, this.month().month));
  protected readonly days = computed(() => monthCells(this.month().year, this.month().month));
  protected readonly canGoBack = computed(() => {
    const current = monthOf(this.today);
    const shown = this.month();
    return shown.year > current.year || (shown.year === current.year && shown.month > current.month);
  });
  protected readonly slots = computed(() => (this.availability.hasValue() ? this.availability.value() : []));
  protected readonly quote = computed(() =>
    this.quoteResource.hasValue() ? this.quoteResource.value() : null,
  );
  protected readonly sameDayWait = computed(() => this.date() === this.today);

  constructor() {
    effect(() => {
      if (
        this.courtsResource.status() === 'error' ||
        this.availability.status() === 'error' ||
        this.quoteResource.status() === 'error'
      ) {
        this.notice.error('No se pudo conectar con el servidor');
      }
    });
  }

  protected chooseCourt(court: Court): void {
    this.courtId.set(court.id);
    this.start.set(null);
  }

  protected clearFilters(): void {
    this.query.set('');
    this.sport.set('');
  }

  protected goTo(step: number): void {
    if (step < this.step()) {
      this.step.set(step);
    }
  }

  protected continueFromCourt(): void {
    if (this.court() !== null) {
      this.step.set(1);
    }
  }

  protected previousMonth(): void {
    if (this.canGoBack()) {
      this.month.update((current) => shiftMonth(current.year, current.month, -1));
    }
  }

  protected nextMonth(): void {
    this.month.update((current) => shiftMonth(current.year, current.month, 1));
  }

  protected chooseDate(iso: string): void {
    this.date.set(iso);
    this.start.set(null);
  }

  protected changeDuration(hours: number): void {
    this.duration.set(hours);
    this.start.set(null);
  }

  protected chooseStart(startsAt: string): void {
    this.start.set(startsAt);
  }

  protected continueFromSchedule(): void {
    if (this.start() !== null) {
      this.step.set(2);
    }
  }

  protected back(): void {
    this.step.update((current) => Math.max(0, current - 1));
  }

  protected async reserve(): Promise<void> {
    const court = this.court();
    const start = this.start();
    if (court === null || start === null || this.busy()) {
      return;
    }

    this.busy.set(true);
    try {
      const saved = await firstValueFrom(
        this.reservations.create({
          courtId: court.id,
          date: this.date(),
          startsAt: start,
          durationHours: this.duration(),
        }),
      );
      this.notice.success(
        saved.status === 'WAITING' ? 'Quedaste en lista de espera.' : 'Reserva confirmada.',
      );
      this.step.set(0);
      this.courtId.set(null);
      this.start.set(null);
    } catch (error: unknown) {
      this.notice.error(messageOf(error));
    } finally {
      this.busy.set(false);
    }
  }
}

function monthOf(iso: string): { year: number; month: number } {
  const [year, month] = iso.split('-');
  return { year: Number(year), month: Number(month) };
}

function messageOf(error: unknown): string {
  return httpMessage(error, {
    409: 'Ese horario no está disponible.',
    400: 'Revisa la fecha, la hora y la duración.',
  });
}
