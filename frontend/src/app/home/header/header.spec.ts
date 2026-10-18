import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Header } from './header';

describe('Header', () => {
  let component: Header;
  let fixture: ComponentFixture<Header>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Header],
    }).compileComponents();

    fixture = TestBed.createComponent(Header);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows the logo and the session buttons', () => {
    const image: HTMLImageElement = fixture.nativeElement.querySelector('img');
    const text = fixture.nativeElement.textContent;

    expect(image.alt).toBe('Deportal');
    expect(image.getAttribute('src')).toContain('logo.webp');
    expect(text).toContain('Iniciar sesión');
    expect(text).toContain('Registrarse');
  });
});
