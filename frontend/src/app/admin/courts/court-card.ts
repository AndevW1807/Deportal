import { Component, computed, input, output } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { Court, capacityLabel, formatRate, openSpan, sportLook } from './court.model';

@Component({
  selector: 'app-court-card',
  imports: [MatIcon],
  templateUrl: './court-card.html',
  styleUrl: './court-card.css',
})
export class CourtCard {
  readonly court = input.required<Court>();
  readonly leaving = input(false);
  readonly edit = output<Court>();
  readonly remove = output<Court>();

  protected readonly look = computed(() => sportLook(this.court().sportType));
  protected readonly span = computed(() => openSpan(this.court().opensAt, this.court().closesAt));
  protected readonly rate = computed(() => formatRate(this.court().hourlyRate));
  protected readonly people = computed(() => capacityLabel(this.court().capacity));
}
