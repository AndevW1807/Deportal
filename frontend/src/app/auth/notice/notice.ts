import { Service, signal } from '@angular/core';

export type NoticeKind = 'success' | 'error';

@Service()
export class Notice {
  private timer = 0;
  private readonly message = signal<string | null>(null);
  private readonly tone = signal<NoticeKind>('success');
  readonly text = this.message.asReadonly();
  readonly kind = this.tone.asReadonly();

  success(text: string): void {
    this.show(text, 'success');
  }

  error(text: string): void {
    this.show(text, 'error');
  }

  clear(): void {
    window.clearTimeout(this.timer);
    this.message.set(null);
  }

  private show(text: string, kind: NoticeKind): void {
    window.clearTimeout(this.timer);
    this.tone.set(kind);
    this.message.set(text);
    this.timer = window.setTimeout(() => this.message.set(null), 6000);
  }
}
