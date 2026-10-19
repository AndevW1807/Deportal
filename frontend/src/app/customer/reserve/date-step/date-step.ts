import { Component, input, output } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { formatDay } from '../../shared/reservation.model';

@Component({
  selector: 'app-reserve-date-step',
  imports: [MatIcon],
  templateUrl: './date-step.html',
  styleUrl: '../reserve.css',
  host: { style: 'display: contents' },
})
export class ReserveDateStep {
  readonly monthLabel = input.required<string>();
  readonly canGoBack = input.required<boolean>();
  readonly days = input.required<({ iso: string; day: number } | null)[]>();
  readonly date = input.required<string>();
  readonly today = input.required<string>();
  readonly previousMonth = output<void>();
  readonly nextMonth = output<void>();
  readonly dateChosen = output<string>();
  protected readonly weekdays = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
  protected readonly formatDay = formatDay;

  protected past(iso: string): boolean {
    return iso < this.today();
  }

  protected choose(iso: string): void {
    if (!this.past(iso)) {
      this.dateChosen.emit(iso);
    }
  }
}
