import { NgOptimizedImage } from '@angular/common';
import { BreakpointObserver } from '@angular/cdk/layout';
import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatIconButton } from '@angular/material/button';
import { MatIcon } from '@angular/material/icon';
import { MatListItem, MatListItemIcon, MatListItemTitle, MatNavList } from '@angular/material/list';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { MatToolbar } from '@angular/material/toolbar';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { catchError, filter, map, of } from 'rxjs';
import { AuthService } from '../../auth/session/auth-service';
import { SessionUser } from '../../auth/session/session.model';

interface MenuItem {
  label: string;
  icon: string;
  link: string;
  roles: readonly SessionUser['role'][];
}

const MENU: readonly MenuItem[] = [
  { label: 'Canchas', icon: 'stadium', link: '/admin/courts', roles: ['ADMIN'] },
  { label: 'Usuarios', icon: 'group', link: '/admin/users', roles: ['ADMIN'] },
];

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
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly sessionUser = toSignal(this.auth.session().pipe(catchError(() => of(null))), {
    initialValue: undefined,
  });
  private readonly path = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map(() => this.router.url),
    ),
    { initialValue: this.router.url },
  );
  private readonly narrow = toSignal(
    inject(BreakpointObserver)
      .observe('(max-width: 1100px)')
      .pipe(map((result) => result.matches)),
    { initialValue: false },
  );

  protected readonly isNarrow = this.narrow;
  protected readonly opened = signal(true);
  protected readonly profileName = computed(() => this.sessionUser()?.name.trim() ?? '');
  protected readonly menu = computed(() => {
    const role = this.sessionUser()?.role;
    return MENU.filter((item) => role !== undefined && item.roles.includes(role));
  });
  protected readonly pageTitle = computed(
    () => this.menu().find((item) => this.path().startsWith(item.link))?.label ?? '',
  );
  protected readonly homeLink = computed(() =>
    this.sessionUser()?.role === 'ADMIN' ? '/admin/courts' : '/app',
  );

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
