import { Service, signal } from '@angular/core';

@Service()
export class Notice {
  private timer = 0;
  private readonly message = signal<string | null>(null);
  readonly text = this.message.asReadonly();

  success(text: string): void {
    window.clearTimeout(this.timer);
    this.message.set(text);
    this.timer = window.setTimeout(() => this.message.set(null), 6000);
  }
}
