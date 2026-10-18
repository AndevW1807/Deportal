import { NgOptimizedImage } from '@angular/common';
import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatListItem, MatListItemIcon, MatListItemTitle, MatNavList } from '@angular/material/list';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { MatToolbar } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthApi } from '../../auth/auth-api';

@Component({
  imports: [
    NgOptimizedImage,
    MatSidenavContainer,
    MatSidenav,
    MatSidenavContent,
    MatToolbar,
    MatNavList,
    MatListItem,
    MatListItemIcon,
    MatListItemTitle,
    MatIconButton,
    MatIcon,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
  ],
  selector: 'app-admin-shell',
  styleUrl: './admin-shell.css',
  templateUrl: './admin-shell.html',
})
export class AdminShell {
  private readonly auth = inject(AuthApi);
  private readonly sessionUser = toSignal(this.auth.session().pipe(catchError(() => of(null))), {
    initialValue: undefined,
  });
  private readonly narrow = toSignal(
    inject(BreakpointObserver)
      .observe('(max-width: 1100px)')
      .pipe(map((result) => result.matches)),
    { initialValue: false },
  );

  protected readonly isNarrow = this.narrow;
  protected readonly opened = signal(true);
  protected readonly profileName = computed(() => this.sessionUser()?.name.trim() ?? '');

  constructor() {
    effect(() => {
      const narrow = this.narrow();
      untracked(() => this.opened.set(!narrow));
    });
  }

  protected toggleMenu(): void {
    this.opened.update((open) => !open);
  }

  protected closeIfNarrow(): void {
    if (this.isNarrow()) {
      this.opened.set(false);
    }
  }
}
