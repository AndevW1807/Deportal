import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { authInterceptor } from '../../auth/session/auth-interceptor';
import { formatCop } from '../courts/court.model';
import { Reports } from './reports';

describe('Reports', () => {
  let fixture: ComponentFixture<Reports>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Reports],
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Reports);
    fixture.detectChanges();
  });

  afterEach(() => {
    http.verify();
  });

  it('shows the current month with available hours and zero reservations', async () => {
    const range = currentMonth();
    flushSports();
    const report = http.expectOne(
      (request) => request.url === '/api/admin/reports/utilization',
    );
    expect(report.request.params.get('from')).toBe(range.from);
    expect(report.request.params.get('to')).toBe(range.to);
    expect(report.request.params.has('sportTypeCode')).toBe(false);
    report.flush({
      reservations: 0,
      reservedHours: 0,
      income: 0,
      occupancyRate: 0,
      courts: [
        {
          id: 1,
          name: 'Norte',
          sportName: 'Fútbol',
          reservations: 0,
          reservedHours: 0,
          availableHours: 496,
          occupancyRate: 0,
          income: 0,
        },
      ],
    });
    await fixture.whenStable();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Total de reservaciones');
    expect(text).toContain('0,00 h');
    expect(text).toContain(formatCop(0));
    expect(text).toContain('0 %');
    expect(text).toContain('Sin reservaciones en el período');
    expect(text).toContain('Norte');
    expect(text).toContain('496,00 h');
    expect(text).not.toContain('Acciones');
  });

  it('asks again with the chosen sport', async () => {
    flushSports([{ code: 'FOOTBALL', name: 'Fútbol' }]);
    http.expectOne((request) => request.url === '/api/admin/reports/utilization').flush(emptyBody());
    await fixture.whenStable();

    const sport: HTMLSelectElement = fixture.nativeElement.querySelector('select');
    sport.value = 'FOOTBALL';
    sport.dispatchEvent(new Event('change'));
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    button.click();
    fixture.detectChanges();

    const report = http.expectOne((request) => request.url === '/api/admin/reports/utilization');
    expect(report.request.params.get('sportTypeCode')).toBe('FOOTBALL');
    report.flush(emptyBody());
    await fixture.whenStable();
  });

  it('keeps the previous report when the range is inverted', async () => {
    flushSports();
    http.expectOne((request) => request.url === '/api/admin/reports/utilization').flush(emptyBody());
    await fixture.whenStable();

    const [from, to]: NodeListOf<HTMLInputElement> = fixture.nativeElement.querySelectorAll('input');
    from.value = '2026-10-31';
    from.dispatchEvent(new Event('input'));
    to.value = '2026-10-01';
    to.dispatchEvent(new Event('input'));
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    button.click();
    await fixture.whenStable();

    http.expectNone((request) => request.url === '/api/admin/reports/utilization');
    expect(fixture.nativeElement.textContent).toContain(
      'La fecha final debe ser igual o posterior a la inicial.',
    );
  });

  function flushSports(sports: { code: string; name: string }[] = []): void {
    http.expectOne('/api/admin/sport-types').flush(sports);
  }
});

function emptyBody(): object {
  return { reservations: 0, reservedHours: 0, income: 0, occupancyRate: 0, courts: [] };
}

function currentMonth(): { from: string; to: string } {
  const today = new Date();
  const from = new Date(today.getFullYear(), today.getMonth(), 1);
  const to = new Date(today.getFullYear(), today.getMonth() + 1, 0);
  return { from: iso(from), to: iso(to) };
}

function iso(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}
