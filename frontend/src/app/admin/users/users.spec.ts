import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { authInterceptor } from '../../auth/session/auth-interceptor';
import { Notice } from '../../auth/notice/notice';
import { Users } from './users';

describe('Users', () => {
  let fixture: ComponentFixture<Users>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Users],
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Users);
    fixture.detectChanges();
  });

  afterEach(() => {
    http.verify();
  });

  it('shows customers and their membership switch', async () => {
    http.expectOne('/api/admin/users').flush([
      { id: 2, name: 'Ana', email: 'ana@deportal.test', membership: 'MEMBER' },
      { id: 3, name: 'Luis', email: 'luis@deportal.test', membership: 'NON_MEMBER' },
    ]);
    await fixture.whenStable();

    const text = fixture.nativeElement.textContent as string;
    const switches: NodeListOf<HTMLElement> = fixture.nativeElement.querySelectorAll('mat-slide-toggle');

    expect(text).toContain('Usuarios');
    expect(text).toContain('2 clientes registrados');
    expect(text).toContain('Ana');
    expect(text).toContain('ana@deportal.test');
    expect(text).toContain('Miembro');
    expect(text).toContain('No miembro');
    expect(switches.length).toBe(2);
    expect(switches[0].classList.contains('mat-mdc-slide-toggle-checked')).toBe(true);
  });

  it('filters customers by name', async () => {
    http.expectOne('/api/admin/users').flush([
      { id: 2, name: 'Ana', email: 'ana@deportal.test', membership: 'MEMBER' },
      { id: 3, name: 'Luis', email: 'luis@deportal.test', membership: 'NON_MEMBER' },
    ]);
    await fixture.whenStable();

    const search: HTMLInputElement = fixture.nativeElement.querySelector(
      'input[aria-label="Buscar por nombre"]',
    );
    search.value = 'lu';
    search.dispatchEvent(new Event('input'));
    await fixture.whenStable();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Luis');
    expect(text).not.toContain('Ana');
    expect(fixture.nativeElement.querySelectorAll('mat-slide-toggle').length).toBe(1);

    fixture.nativeElement.querySelector('button.clear').click();
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Ana');
    expect(fixture.nativeElement.querySelectorAll('mat-slide-toggle').length).toBe(2);
  });

  it('saves the membership when the switch changes', async () => {
    http.expectOne('/api/admin/users').flush([
      { id: 3, name: 'Luis', email: 'luis@deportal.test', membership: 'NON_MEMBER' },
    ]);
    await fixture.whenStable();

    fixture.nativeElement.querySelector('mat-slide-toggle button').click();
    await fixture.whenStable();

    const request = http.expectOne({ method: 'PUT', url: '/api/admin/users/3' });
    expect(request.request.body).toEqual({ membership: 'MEMBER' });
    request.flush({ id: 3, name: 'Luis', email: 'luis@deportal.test', membership: 'MEMBER' });
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Miembro');
  });

  it('restores the switch and reports an error when the update fails', async () => {
    http.expectOne('/api/admin/users').flush([
      { id: 3, name: 'Luis', email: 'luis@deportal.test', membership: 'NON_MEMBER' },
    ]);
    await fixture.whenStable();

    fixture.nativeElement.querySelector('mat-slide-toggle button').click();
    await fixture.whenStable();

    http.expectOne('/api/admin/users/3').flush({}, { status: 500, statusText: 'Error' });
    await nextTurn();
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('No miembro');
    expect(TestBed.inject(Notice).text()).toBe('No se pudo actualizar la membresía');
    expect(TestBed.inject(Notice).kind()).toBe('error');
  });

  it('shows an empty list', async () => {
    http.expectOne('/api/admin/users').flush([]);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Aún no hay clientes registrados.');
  });
});

function nextTurn(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 0));
}
