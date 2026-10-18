import { httpResource } from '@angular/common/http';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { type EChartsCoreOption } from 'echarts/core';
import { Notice } from '../../auth/notice/notice';
import { parseSportTypes } from '../courts/court-service';
import { SportType } from '../courts/court.model';
import { ReportChart } from './report-chart/report-chart';
import {
  emptyReport,
  formatHours,
  formatIncome,
  formatOccupancy,
  parseReport,
  UtilizationReport,
} from './report.model';

interface ReportQuery {
  from: string;
  to: string;
  sportTypeCode: string;
}

@Component({
  selector: 'app-reports',
  imports: [ReportChart],
  templateUrl: './reports.html',
  styleUrl: './reports.css',
})
export class Reports {
  private readonly notice = inject(Notice);

  protected readonly draftFrom = signal(monthRange().from);
  protected readonly draftTo = signal(monthRange().to);
  protected readonly draftSport = signal('');
  protected readonly rangeError = signal('');
  private readonly query = signal<ReportQuery>(monthRange());

  protected readonly sportsResource = httpResource(() => '/api/admin/sport-types', {
    defaultValue: [] as SportType[],
    parse: parseSportTypes,
  });
  protected readonly reportResource = httpResource(() => reportRequest(this.query()), {
    defaultValue: emptyReport,
    parse: parseReport,
  });

  protected readonly sports = computed(() =>
    this.sportsResource.hasValue() ? this.sportsResource.value() : [],
  );
  protected readonly report = computed(() =>
    this.reportResource.hasValue() ? this.reportResource.value() : emptyReport,
  );
  protected readonly loading = computed(() => this.reportResource.status() === 'loading');
  protected readonly cards = computed(() => [
    { label: 'Total de reservaciones', value: String(this.report().reservations) },
    { label: 'Horas reservadas', value: formatHours(this.report().reservedHours) },
    { label: 'Ingresos', value: formatIncome(this.report().income) },
    { label: 'Tasa de ocupación promedio', value: formatOccupancy(this.report().occupancyRate) },
  ]);
  protected readonly barOption = computed(() => barOption(this.report()));
  protected readonly donutOption = computed(() => donutOption(this.report()));
  protected readonly withoutReservations = computed(() => this.report().reservations === 0);
  protected readonly formatHours = formatHours;
  protected readonly formatIncome = formatIncome;
  protected readonly formatOccupancy = formatOccupancy;

  constructor() {
    effect(() => {
      if (this.reportResource.status() === 'error') {
        this.notice.error('No se pudo cargar el reporte');
      }
    });
  }

  protected onFrom(event: Event): void {
    this.draftFrom.set(inputValue(event));
  }

  protected onTo(event: Event): void {
    this.draftTo.set(inputValue(event));
  }

  protected onSport(event: Event): void {
    this.draftSport.set(inputValue(event));
  }

  protected generate(): void {
    if (this.draftTo() < this.draftFrom()) {
      this.rangeError.set('La fecha final debe ser igual o posterior a la inicial.');
      return;
    }
    this.rangeError.set('');
    this.query.set({
      from: this.draftFrom(),
      to: this.draftTo(),
      sportTypeCode: this.draftSport(),
    });
  }
}

function monthRange(): ReportQuery {
  const today = new Date();
  const from = new Date(today.getFullYear(), today.getMonth(), 1);
  const to = new Date(today.getFullYear(), today.getMonth() + 1, 0);
  return { from: isoDate(from), to: isoDate(to), sportTypeCode: '' };
}

function isoDate(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}

function reportRequest(query: ReportQuery): { url: string; params: Record<string, string> } {
  const params: Record<string, string> = { from: query.from, to: query.to };
  if (query.sportTypeCode) {
    params['sportTypeCode'] = query.sportTypeCode;
  }
  return { url: '/api/admin/reports/utilization', params };
}

function inputValue(event: Event): string {
  return (event.target as HTMLInputElement).value;
}

function barOption(report: UtilizationReport): EChartsCoreOption {
  return {
    color: ['#0f6b3c', '#c5d5cc'],
    tooltip: { trigger: 'axis' },
    legend: { top: 0, data: ['Horas reservadas', 'Horas disponibles'] },
    grid: { left: 8, right: 16, top: 48, bottom: 4, containLabel: true },
    xAxis: {
      type: 'category',
      data: report.courts.map((court) => court.name),
      axisLabel: { interval: 0 },
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: 'Horas reservadas',
        type: 'bar',
        data: report.courts.map((court) => court.reservedHours),
      },
      {
        name: 'Horas disponibles',
        type: 'bar',
        data: report.courts.map((court) => court.availableHours),
      },
    ],
  };
}

function donutOption(report: UtilizationReport): EChartsCoreOption {
  return {
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: ['48%', '72%'],
        data: report.courts.map((court) => ({ name: court.name, value: court.reservations })),
      },
    ],
  };
}
