import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { authInterceptor } from '../../auth/session/auth-interceptor';
import { Court } from '../../shared/court/court.model';
import { formatCop } from '../../shared/format/money';
import { Reserve } from './reserve';

describe('Reserve', () => {
  let fixture: ComponentFixture<Reserve>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Reserve],
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Reserve);
    fixture.detectChanges();
    flushOpen();
    await fixture.whenStable();
  });

  afterEach(() => {
    http.verify();
  });

  it('shows the bookable courts and filters them', () => {
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Reservar cancha');
    expect(text).toContain('Norte');
    expect(text).toContain('Sur');

    const sport = fixture.nativeElement.querySelector('select[aria-label="Filtrar por deporte"]') as HTMLSelectElement;
    sport.value = 'TENNIS';
    sport.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Sur');
    expect(fixture.nativeElement.textContent).not.toContain('Norte');
  });

  it('reserves a free slot and shows the member discount', async () => {
    clickCourt('Norte');
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.primary').click();
    fixture.detectChanges();
    flushAvailability('FREE');
    await fixture.whenStable();

    const hour = buttonByText('16:00');
    hour.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('1 hora de limpieza');
    fixture.nativeElement.querySelectorAll('.primary')[0].click();
    fixture.detectChanges();
    const quote = http.expectOne((call) => call.url.startsWith('/api/reservations/quote'));
    expect(quote.request.urlWithParams).toContain('courtId=4');
    expect(quote.request.urlWithParams).toContain('startsAt=16');
    expect(quote.request.urlWithParams).toContain('durationHours=1');
    quote.flush({
      subtotal: '20.00',
      memberDiscount: '2.00',
      offPeakDiscount: '0.00',
      total: '18.00',
      endsAt: '17:00',
    });
    await fixture.whenStable();
    fixture.detectChanges();

    const summary = fixture.nativeElement.textContent as string;
    expect(summary).toContain('Descuento de miembro');
    expect(summary).toContain('Fuera de hora pico');
    expect(summary).toContain(formatCop(2));
    expect(summary).toContain(formatCop(0));
    expect(summary).toContain(formatCop(18));
    fixture.nativeElement.querySelectorAll('.primary')[0].click();
    const create = http.expectOne('/api/reservations');
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toEqual({
      courtId: 4,
      date: expect.any(String),
      startsAt: '16:00',
      durationHours: 1,
    });
    create.flush(reservation('CONFIRMED'));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).not.toContain('Tus reservas');
    expect(fixture.nativeElement.textContent).toContain('Reservar cancha');
  });

  function flushOpen(): void {
    http.expectOne('/api/reservations/courts').flush([
      court(4, 'Norte', 'FOOTBALL', 'Fútbol'),
      court(5, 'Sur', 'TENNIS', 'Tenis'),
    ]);
  }

  function flushAvailability(state: 'FREE' | 'OCCUPIED' | 'BLOCKED'): void {
    const request = http.expectOne((call) => call.url.startsWith('/api/reservations/availability'));
    request.flush([{ startsAt: '16:00', state }]);
  }

  function clickCourt(name: string): void {
    const button = [...fixture.nativeElement.querySelectorAll('.card')].find((item) =>
      (item as HTMLElement).textContent?.includes(name),
    ) as HTMLButtonElement;
    button.click();
  }

  function buttonByText(text: string): HTMLButtonElement {
    const button = [...fixture.nativeElement.querySelectorAll('.starts button')].find((item) =>
      (item as HTMLElement).textContent?.includes(text),
    );
    return button as HTMLButtonElement;
  }

  function court(id: number, name: string, code: string, sport: string): Court {
    return {
      id,
      name,
      sportType: { code, name: sport },
      capacity: 10,
      opensAt: '06:00',
      closesAt: '22:00',
      hourlyRate: '20.00',
    };
  }

  function reservation(status: 'CONFIRMED' | 'WAITING' | 'CANCELLED') {
    return {
      id: 9,
      courtId: 4,
      courtName: 'Norte',
      date: '2026-10-19',
      startsAt: '16:00',
      durationHours: 1,
      endsAt: '17:00',
      status,
      hourlyRate: '20.00',
      discountRate: '0.10',
      subtotal: '20.00',
      discountAmount: '2.00',
      total: '18.00',
    };
  }
});
