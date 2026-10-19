import { Component, input, output } from '@angular/core';
import { Slot } from '../../shared/reservation.model';

@Component({
  selector: 'app-reserve-hours-step',
  templateUrl: './hours-step.html',
  styleUrl: '../reserve.css',
  host: { style: 'display: contents' },
})
export class ReserveHoursStep {
  readonly duration = input.required<number>();
  readonly sameDayWait = input.required<boolean>();
  readonly slots = input.required<Slot[]>();
  readonly start = input.required<string | null>();
  readonly durationChange = output<number>();
  readonly startChosen = output<string>();
  protected readonly durations = [1, 2, 3, 4, 5, 6, 7, 8];

  protected onDuration(event: Event): void {
    const inputElement = event.target;
    if (inputElement instanceof HTMLSelectElement) {
      this.durationChange.emit(Number(inputElement.value));
    }
  }

  protected selectable(slot: Slot): boolean {
    if (slot.state === 'FREE') {
      return true;
    }

    return slot.state === 'OCCUPIED' && this.sameDayWait();
  }

  protected choose(slot: Slot): void {
    if (this.selectable(slot)) {
      this.startChosen.emit(slot.startsAt);
    }
  }
}
