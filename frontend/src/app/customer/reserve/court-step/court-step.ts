import { Component, input, output } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { Court, capacityLabel, formatRate, sportLook } from '../../../shared/court/court.model';

@Component({
  selector: 'app-reserve-court-step',
  imports: [MatIcon],
  templateUrl: './court-step.html',
  styleUrl: '../reserve.css',
  host: { style: 'display: contents' },
})
export class ReserveCourtStep {
  readonly courts = input.required<Court[]>();
  readonly visibleCourts = input.required<Court[]>();
  readonly query = input.required<string>();
  readonly sport = input.required<string>();
  readonly sports = input.required<readonly { code: string; name: string }[]>();
  readonly selectedId = input.required<number | null>();
  readonly queryChange = output<string>();
  readonly sportChange = output<string>();
  readonly filtersCleared = output<void>();
  readonly courtChosen = output<Court>();
  readonly continued = output<void>();
  protected readonly formatRate = formatRate;
  protected readonly capacityLabel = capacityLabel;
  protected readonly sportLook = sportLook;

  protected onQuery(event: Event): void {
    const inputElement = event.target;
    if (inputElement instanceof HTMLInputElement) {
      this.queryChange.emit(inputElement.value);
    }
  }

  protected onSport(event: Event): void {
    const inputElement = event.target;
    if (inputElement instanceof HTMLSelectElement) {
      this.sportChange.emit(inputElement.value);
    }
  }
}
