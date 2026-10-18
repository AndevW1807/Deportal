import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HomeFooter } from './home-footer';

describe('HomeFooter', () => {
  let component: HomeFooter;
  let fixture: ComponentFixture<HomeFooter>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeFooter],
    }).compileComponents();

    fixture = TestBed.createComponent(HomeFooter);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('invites people to join the community', () => {
    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Más que canchas');
    expect(text).toContain('una comunidad');
    expect(text).toContain('Únete ahora');
    expect(text).toContain('Conoce nuevos deportistas');
    expect(text).toContain('Organiza partidos y eventos');
    expect(text).toContain('Forma parte de una comunidad activa');
  });
});
