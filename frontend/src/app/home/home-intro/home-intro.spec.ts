import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HomeIntro } from './home-intro';

describe('HomeIntro', () => {
  let component: HomeIntro;
  let fixture: ComponentFixture<HomeIntro>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeIntro],
    }).compileComponents();

    fixture = TestBed.createComponent(HomeIntro);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows the same intro copy', () => {
    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Tu deporte, sin complicaciones');
    expect(text).toContain('Reserva tu cancha');
    expect(text).toContain('en minutos');
    expect(text).toContain('sin conflictos de horario');
    expect(text).toContain('Registrarse');
    expect(text).toContain('Iniciar sesión');
    expect(text).toContain('Disponibilidad en tiempo real');
    expect(text).toContain('Pagos seguros y descuentos');
    expect(text).toContain('Prioridad para miembros');
  });
});
