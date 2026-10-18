import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HomeSports } from './home-sports';

describe('HomeSports', () => {
  let component: HomeSports;
  let fixture: ComponentFixture<HomeSports>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeSports],
    }).compileComponents();

    fixture = TestBed.createComponent(HomeSports);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('lists the five sports', () => {
    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Canchas para');
    expect(text).toContain('Fútbol');
    expect(text).toContain('Básquet');
    expect(text).toContain('Tenis');
    expect(text).toContain('Voleibol');
    expect(text).toContain('Multiusos');
  });
});
