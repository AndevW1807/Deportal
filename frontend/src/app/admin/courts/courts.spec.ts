import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { authInterceptor } from '../../auth/session/auth-interceptor';
import { Notice } from '../../auth/notice/notice';
import { Court, formatRate } from './court.model';
import { Courts } from './courts';

describe('Courts', () => {
  let fixture: ComponentFixture<Courts>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Courts],
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Courts);
    fixture.detectChanges();
  });

  afterEach(() => {
    TestBed.inject(MatDialog).closeAll();
    http.verify();
  });

  it('shows the registered courts', async () => {
    flushCourts([court('Norte', 'FOOTBALL', 'Fútbol'), court('Sur', 'TENNIS', 'Tenis', 5)]);
    await fixture.whenStable();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Canchas');
    expect(text).toContain('2 canchas registradas');
    expect(text).toContain('Norte');
    expect(text).toContain('Sur');
    expect(text).toContain(formatRate('20.00'));
    expect(fixture.nativeElement.querySelector('button[aria-label="Editar Norte"]')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('button[aria-label="Eliminar Norte"]')).not.toBeNull();
  });

  it('filters by sport and by name', async () => {
    flushCourts([court('Norte', 'FOOTBALL', 'Fútbol'), court('Sur', 'TENNIS', 'Tenis', 5)]);
    await fixture.whenStable();

    chooseSport('TENNIS');
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Sur');
    expect(fixture.nativeElement.textContent).not.toContain('Norte');

    chooseSport('');
    fill(fixture.nativeElement.querySelector('input[aria-label="Buscar por nombre"]'), 'nor');
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Norte');
    expect(fixture.nativeElement.textContent).not.toContain('Sur');
  });

  it('shows an empty state when there are no courts', async () => {
    flushCourts([]);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Aún no hay canchas registradas');
    expect(fixture.nativeElement.textContent).toContain('0 canchas registradas');
  });

  it('updates the card after a successful edit', async () => {
    flushCourts([court('Norte', 'FOOTBALL', 'Fútbol')]);
    await fixture.whenStable();

    fixture.nativeElement.querySelector('button[aria-label="Editar Norte"]').click();
    await fixture.whenStable();
    const dialog = document.querySelector('app-court-editor');
    expect(dialog).not.toBeNull();
    fill(dialog?.querySelector('input[aria-label="Nombre"]') as HTMLInputElement, 'Cancha Sur');
    dialog?.querySelector('form')?.dispatchEvent(new Event('submit'));
    await nextTurn();

    const update = http.expectOne('/api/admin/courts/4');
    expect(update.request.method).toBe('PUT');
    expect(update.request.body).toMatchObject({
      name: 'Cancha Sur',
      sportTypeCode: 'FOOTBALL',
      capacity: 10,
      opensAt: '06:00',
      closesAt: '22:00',
      hourlyRate: '20.00',
    });
    update.flush(court('Cancha Sur', 'FOOTBALL', 'Fútbol'));
    await new Promise((resolve) => setTimeout(resolve, 150));
    await fixture.whenStable();

    expect(TestBed.inject(Notice).text()).toBe('Cancha actualizada');
    expect(document.querySelector('app-court-editor')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Cancha Sur');
    expect(TestBed.inject(Notice).kind()).toBe('success');
  });

  it('adds a court from the dialog', async () => {
    flushCourts([court('Cancha Norte', 'FOOTBALL', 'Fútbol')]);
    await fixture.whenStable();

    fixture.nativeElement.querySelector('button.add').click();
    await fixture.whenStable();
    const dialog = document.querySelector('app-court-editor') as HTMLElement;
    fill(dialog.querySelector('input[aria-label="Nombre"]') as HTMLInputElement, 'Cancha Sur');
    dialog.querySelector('form')?.dispatchEvent(new Event('submit'));
    await nextTurn();

    const request = http.expectOne({ method: 'POST', url: '/api/admin/courts' });
    expect(request.request.body).toMatchObject({ name: 'Cancha Sur', sportTypeCode: 'FOOTBALL' });
    request.flush({
      id: 9,
      name: 'Cancha Sur',
      sportType: { code: 'FOOTBALL', name: 'Fútbol' },
      capacity: 10,
      opensAt: '06:00',
      closesAt: '22:00',
      hourlyRate: '5.00',
    });
    await nextTurn();
    await new Promise((resolve) => setTimeout(resolve, 150));
    await fixture.whenStable();

    expect(TestBed.inject(Notice).text()).toBe('Cancha creada');
    expect(document.querySelector('app-court-editor')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Cancha Sur');
  });

  it('keeps letters and extra digits out of capacity and rate', async () => {
    flushCourts([court('Norte', 'FOOTBALL', 'Fútbol')]);
    await fixture.whenStable();

    fixture.nativeElement.querySelector('button.add').click();
    await fixture.whenStable();
    const dialog = document.querySelector('app-court-editor') as HTMLElement;
    const capacity = dialog.querySelector('input[aria-label="Capacidad"]') as HTMLInputElement;
    const rate = dialog.querySelector('input[aria-label="Tarifa por hora"]') as HTMLInputElement;

    expect(blocked(capacity, 'a')).toBe(true);
    capacity.value = '1';
    capacity.setSelectionRange(1, 1);
    expect(blocked(capacity, '5')).toBe(false);
    capacity.value = '50';
    capacity.setSelectionRange(2, 2);
    expect(blocked(capacity, '0')).toBe(true);
    capacity.value = '5';
    capacity.setSelectionRange(1, 1);
    expect(blocked(capacity, '6')).toBe(true);
    expect(blocked(rate, 'e')).toBe(true);
    expect(blocked(rate, 'x')).toBe(true);
    expect(blocked(rate, '000000000')).toBe(true);
    expect(blocked(rate, '.999')).toBe(true);
  });

  it('keeps the editor open when the name is already used', async () => {
    flushCourts([court('Norte', 'FOOTBALL', 'Fútbol')]);
    await fixture.whenStable();

    fixture.nativeElement.querySelector('button[aria-label="Editar Norte"]').click();
    await fixture.whenStable();
    document.querySelector('app-court-editor form')?.dispatchEvent(new Event('submit'));
    await nextTurn();

    http.expectOne('/api/admin/courts/4').flush(
      { detail: 'Court name already registered' },
      { status: 409, statusText: 'Conflict' },
    );
    await nextTurn();
    await fixture.whenStable();

    expect(document.querySelector('app-court-editor')).not.toBeNull();
    expect(document.body.textContent).toContain('Ya existe una cancha con ese nombre');
    expect(TestBed.inject(Notice).kind()).toBe('error');
  });

  it('removes the card after the deletion is confirmed', async () => {
    flushCourts([court('Norte', 'FOOTBALL', 'Fútbol')]);
    await fixture.whenStable();

    fixture.nativeElement.querySelector('button[aria-label="Eliminar Norte"]').click();
    await fixture.whenStable();
    expect(document.body.textContent).toContain('¿Eliminar cancha?');
    expect(document.body.textContent).toContain('Se eliminará la cancha “Norte”');
    (document.querySelector('app-delete-court button.danger') as HTMLButtonElement).click();
    await fixture.whenStable();
    await nextTurn();

    const removal = http.expectOne('/api/admin/courts/4');
    expect(removal.request.method).toBe('DELETE');
    removal.flush(null, { status: 204, statusText: 'No Content' });
    await new Promise((resolve) => setTimeout(resolve, 220));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).not.toContain('Norte');
    expect(fixture.nativeElement.textContent).toContain('Aún no hay canchas registradas');
    expect(TestBed.inject(Notice).text()).toBe('Cancha eliminada');
  });

  it('shows a retry notice when the list cannot be loaded', async () => {
    http.expectOne('/api/admin/courts').error(new ProgressEvent('error'));
    http.expectOne('/api/admin/sport-types').flush([]);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Reintentar');
    expect(TestBed.inject(Notice).text()).toBe('No se pudo conectar con el servidor');
    expect(TestBed.inject(Notice).kind()).toBe('error');
  });

  function flushCourts(courts: Court[]): void {
    http.expectOne('/api/admin/courts').flush(courts);
    http.expectOne('/api/admin/sport-types').flush([
      { code: 'FOOTBALL', name: 'Fútbol' },
      { code: 'TENNIS', name: 'Tenis' },
    ]);
  }

  function chooseSport(code: string): void {
    const select = fixture.nativeElement.querySelector(
      'select[aria-label="Deporte"]',
    ) as HTMLSelectElement;
    select.value = code;
    select.dispatchEvent(new Event('change'));
  }
});

function court(name: string, code: string, sport: string, id = 4): Court {
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

function fill(input: HTMLInputElement, value: string): void {
  input.value = value;
  input.dispatchEvent(new Event('input'));
}

function blocked(input: HTMLInputElement, data: string): boolean {
  const event = new InputEvent('beforeinput', {
    data,
    inputType: 'insertText',
    cancelable: true,
    bubbles: true,
  });
  input.dispatchEvent(event);
  return event.defaultPrevented;
}

function nextTurn(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 0));
}
