import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { formatCop } from '../../shared/format/money';
import { Notice } from '../../shared/notice/notice';
import { authInterceptor } from '../../auth/session/auth-interceptor';
import { MyReservations } from './reservations';

describe('MyReservations', () => {
  let fixture: ComponentFixture<MyReservations>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyReservations],
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(MyReservations);
    fixture.detectChanges();
  });

  afterEach(() => {
    http.verify();
  });

  it('shows an empty list', async () => {
    http.expectOne('/api/reservations').flush([]);
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Todavía no tienes reservas.');
  });

  it('cancels a future reservation and shows the refund', async () => {
    http.expectOne('/api/reservations').flush([
      reservation(9, true, '20.00'),
      reservation(10, false, null),
    ]);
    await fixture.whenStable();
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Cancha Central');
    expect(text).toContain('Confirmada');
    expect(text).toContain('En lista de espera');
    expect(text).toContain('Reembolso si cancelas');
    expect(text).toContain('Ya empezó');
    const buttons = fixture.nativeElement.querySelectorAll('button') as NodeListOf<HTMLButtonElement>;
    expect(buttons).toHaveLength(2);
    expect(buttons[0].disabled).toBe(false);
    expect(buttons[1].disabled).toBe(true);

    buttons[0].click();
    const cancel = http.expectOne({ method: 'DELETE', url: '/api/reservations/9' });
    cancel.flush({
      ...reservation(9, false, '20.00'),
      status: 'CANCELLED',
      courtId: 4,
      hourlyRate: '20.00',
      discountRate: '0.00',
      subtotal: '20.00',
      discountAmount: '0.00',
    });
    await Promise.resolve();
    fixture.detectChanges();
    http.expectOne('/api/reservations').flush([reservation(10, false, null)]);
    await fixture.whenStable();
    fixture.detectChanges();

    expect(TestBed.inject(Notice).text()).toContain(`Reserva cancelada. Reembolso: ${formatCop(20)}`);
    expect(fixture.nativeElement.textContent).not.toContain('Reembolso si cancelas');
    expect(fixture.nativeElement.textContent).toContain('Ya empezó');
    const remaining = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    expect(remaining.disabled).toBe(true);
  });
});

function reservation(id: number, cancellable: boolean, refundAmount: string | null) {
  return {
    id,
    courtName: id === 9 ? 'Cancha Central' : 'Cancha Norte',
    date: '2026-10-21',
    startsAt: '11:00',
    endsAt: '13:00',
    durationHours: 2,
    status: id === 9 ? 'CONFIRMED' : 'WAITING',
    total: '36.00',
    cancellable,
    refundAmount,
  };
}
