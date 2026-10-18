import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Notice } from './auth/notice/notice';

import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the router outlet', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).querySelector('router-outlet')).not.toBeNull();
  });

  it('shows the success snackbar above the page', () => {
    const fixture = TestBed.createComponent(App);
    TestBed.inject(Notice).success('Registro exitoso. Ya puedes iniciar sesión.');
    fixture.detectChanges();

    const snackbar: HTMLElement | null = fixture.nativeElement.querySelector('[role="status"]');
    expect(snackbar?.textContent).toContain('Registro exitoso. Ya puedes iniciar sesión.');
  });
});
