import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HomeFacts } from './home-facts';

describe('HomeFacts', () => {
  let component: HomeFacts;
  let fixture: ComponentFixture<HomeFacts>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeFacts],
    }).compileComponents();

    fixture = TestBed.createComponent(HomeFacts);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows the four club rules', () => {
    const text = fixture.nativeElement.textContent;

    expect(text).toContain('06:00–22:00');
    expect(text).toContain('Horario');
    expect(text).toContain('1 h');
    expect(text).toContain('Limpieza entre turnos');
    expect(text).toContain('10%');
    expect(text).toContain('Descuento de miembro');
    expect(text).toContain('30%');
    expect(text).toContain('Tope de descuento');
  });
});
