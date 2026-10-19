import { Component, input, output } from '@angular/core';
import { Court, formatRate } from '../../../shared/court/court.model';
import { formatCop } from '../../../shared/format/money';
import { Quote, formatDay } from '../../shared/reservation.model';

@Component({
  selector: 'app-reserve-summary',
  templateUrl: './summary.html',
  styleUrl: '../reserve.css',
  host: { style: 'display: contents' },
})
export class ReserveSummary {
  readonly court = input.required<Court>();
  readonly date = input.required<string>();
  readonly start = input.required<string | null>();
  readonly duration = input.required<number>();
  readonly price = input.required<Quote | null>();
  readonly busy = input.required<boolean>();
  readonly back = output<void>();
  readonly reserved = output<void>();
  protected readonly formatRate = formatRate;
  protected readonly formatCop = formatCop;
  protected readonly formatDay = formatDay;
}
