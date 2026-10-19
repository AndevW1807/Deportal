import { Component, computed, input, output } from '@angular/core';
import { MatButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { Court, capacityLabel, formatRate, sportLook } from '../../../shared/court/court.model';

@Component({
  selector: 'app-court-card',
  imports: [MatIcon, MatButton],
  templateUrl: './court-card.html',
  styleUrl: './court-card.css',
})
export class CourtCard {
  readonly court = input.required<Court>();
  readonly leaving = input(false);
  readonly edit = output<Court>();
  readonly remove = output<Court>();

  protected readonly look = computed(() => sportLook(this.court().sportType));
  protected readonly rate = computed(() => formatRate(this.court().hourlyRate));
  protected readonly people = computed(() => capacityLabel(this.court().capacity));
}
