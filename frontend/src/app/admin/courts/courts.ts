import { HttpErrorResponse, HttpStatusCode, httpResource } from '@angular/common/http';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatIcon } from '@angular/material/icon';
import { firstValueFrom } from 'rxjs';
import { Notice } from '../../auth/notice/notice';
import { CourtCard } from './court-card';
import { CourtEditor } from './court-editor';
import { parseCourts, parseSportTypes, CourtService } from './court-service';
import { Court, sportLook, SportType } from './court.model';
import { DeleteCourtDialog } from './delete-court';

@Component({
  selector: 'app-courts',
  imports: [CourtCard, MatIcon],
  templateUrl: './courts.html',
  styleUrl: './courts.css',
})
export class Courts {
  private readonly dialog = inject(MatDialog);
  private readonly courts = inject(CourtService);
  private readonly notice = inject(Notice);

  protected readonly courtsResource = httpResource(() => '/api/admin/courts', {
    defaultValue: [],
    parse: parseCourts,
  });
  protected readonly sportsResource = httpResource(() => '/api/admin/sport-types', {
    defaultValue: [],
    parse: parseSportTypes,
  });
  protected readonly query = signal('');
  protected readonly sport = signal<string | null>(null);
  protected readonly leavingId = signal<number | null>(null);
  protected readonly skeletons = [0, 1, 2, 3];

  protected readonly sportFilters = computed(() =>
    this.sportsResource.hasValue() ? this.sportsResource.value() : [],
  );
  protected readonly loading = computed(() => this.courtsResource.status() === 'loading');
  protected readonly failed = computed(() => this.courtsResource.status() === 'error');
  protected readonly registered = computed(() =>
    this.courtsResource.hasValue() ? this.courtsResource.value() : [],
  );
  protected readonly visible = computed(() => {
    const text = this.query().trim().toLocaleLowerCase('es');
    const sport = this.sport();

    return this.registered().filter((court) => {
      const matchesName = court.name.toLocaleLowerCase('es').includes(text);
      const matchesSport = sport === null || court.sportType.code === sport;
      return matchesName && matchesSport;
    });
  });
  protected readonly countLabel = computed(() => {
    if (this.loading()) {
      return 'Cargando canchas';
    }

    const total = this.registered().length;
    return total === 1 ? '1 cancha registrada' : `${total} canchas registradas`;
  });

  constructor() {
    effect(() => {
      if (this.courtsResource.status() === 'error') {
        this.notice.error('No se pudo conectar con el servidor');
      }
    });
  }

  protected look(sport: SportType) {
    return sportLook(sport);
  }

  protected delay(index: number): string {
    return `${Math.min(index, 8) * 40}ms`;
  }

  protected onQuery(event: Event): void {
    const target = event.target;
    if (target instanceof HTMLInputElement) {
      this.query.set(target.value);
    }
  }

  protected retry(): void {
    this.notice.clear();
    this.courtsResource.reload();
    this.sportsResource.reload();
  }

  protected edit(court: Court): void {
    const full = window.innerWidth <= 700;
    this.dialog.open(CourtEditor, {
      data: {
        court,
        sportTypes: sportsFor(
          this.sportsResource.hasValue() ? this.sportsResource.value() : [],
          court,
        ),
        onSaved: (updated: Court) => this.replaceCourt(updated),
      },
      width: full ? '100vw' : '30rem',
      maxWidth: full ? '100vw' : '94vw',
      height: full ? '100dvh' : undefined,
      maxHeight: full ? '100dvh' : '92vh',
      autoFocus: 'first-tabbable',
      ariaLabelledBy: 'court-editor-title',
      enterAnimationDuration: 0,
      exitAnimationDuration: 0,
      panelClass: full ? ['court-panel', 'court-panel-full'] : 'court-panel',
    });
  }

  private replaceCourt(updated: Court): void {
    this.courtsResource.update((list) =>
      list
        .map((item) => (item.id === updated.id ? updated : item))
        .sort((left, right) => left.name.localeCompare(right.name, 'es')),
    );
    this.notice.success('Cancha actualizada');
  }

  protected remove(court: Court): void {
    const dialogRef = this.dialog.open(DeleteCourtDialog, {
      data: court,
      width: '26rem',
      maxWidth: '94vw',
      ariaLabelledBy: 'delete-court-title',
      enterAnimationDuration: 0,
      exitAnimationDuration: 0,
      panelClass: 'court-panel',
    });

    void firstValueFrom(dialogRef.afterClosed()).then(async (confirmed) => {
      if (!confirmed) {
        return;
      }

      this.leavingId.set(court.id);
      try {
        await firstValueFrom(this.courts.remove(court.id));
        await pause(180);
        this.courtsResource.update((list) => list.filter((item) => item.id !== court.id));
        this.notice.success('Cancha eliminada');
      } catch (error) {
        const text =
          error instanceof HttpErrorResponse && error.status === HttpStatusCode.Conflict
            ? 'No se puede eliminar: tiene reservas próximas'
            : 'No se pudo conectar con el servidor';
        this.notice.error(text);
      } finally {
        this.leavingId.set(null);
      }
    });
  }
}

function sportsFor(sports: SportType[], court: Court): SportType[] {
  if (sports.some((sport) => sport.code === court.sportType.code)) {
    return sports;
  }

  return [court.sportType, ...sports];
}

function pause(duration: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(resolve, duration);
  });
}
