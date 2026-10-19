import { httpResource } from '@angular/common/http';
import { Component, computed, effect, inject, linkedSignal, signal } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { MatSlideToggle, MatSlideToggleChange } from '@angular/material/slide-toggle';
import { firstValueFrom } from 'rxjs';
import { Notice } from '../../shared/notice/notice';
import { parseCustomers, UserService } from './user-service';
import { Customer } from './user.model';

@Component({
  selector: 'app-users',
  imports: [MatIcon, MatSlideToggle],
  templateUrl: './users.html',
  styleUrls: ['../../shared/page/page.css', './users.css'],
})
export class Users {
  private readonly users = inject(UserService);
  private readonly notice = inject(Notice);
  private readonly pendingId = signal<number | null>(null);
  protected readonly query = signal('');

  protected readonly customersResource = httpResource(() => '/api/admin/users', {
    defaultValue: [],
    parse: parseCustomers,
  });
  protected readonly customers = linkedSignal(() =>
    this.customersResource.hasValue() ? this.customersResource.value() : [],
  );
  protected readonly loading = computed(() => this.customersResource.status() === 'loading');
  protected readonly visible = computed(() => {
    const text = this.query().trim().toLocaleLowerCase('es');
    return this.customers().filter((customer) =>
      customer.name.toLocaleLowerCase('es').includes(text),
    );
  });
  protected readonly filtering = computed(() => this.query().trim().length > 0);
  protected readonly countLabel = computed(() => {
    if (this.loading()) {
      return 'Cargando usuarios';
    }

    const total = this.customers().length;
    return total === 1 ? '1 cliente registrado' : `${total} clientes registrados`;
  });

  constructor() {
    effect(() => {
      if (this.customersResource.status() === 'error') {
        this.notice.error('No se pudo conectar con el servidor');
      }
    });
  }

  protected member(customer: Customer): boolean {
    return customer.membership === 'MEMBER';
  }

  protected label(customer: Customer): string {
    return this.member(customer) ? 'Miembro' : 'No miembro';
  }

  protected busy(id: number): boolean {
    return this.pendingId() === id;
  }

  protected onQuery(event: Event): void {
    const input = event.target;
    if (input instanceof HTMLInputElement) {
      this.query.set(input.value);
    }
  }

  protected clearQuery(): void {
    this.query.set('');
  }

  protected async onMembership(customer: Customer, event: MatSlideToggleChange): Promise<void> {
    const membership = event.checked ? 'MEMBER' : 'NON_MEMBER';
    if (customer.membership === membership || this.pendingId() !== null) {
      return;
    }

    this.pendingId.set(customer.id);
    this.customers.update((rows) =>
      rows.map((row) => (row.id === customer.id ? { ...row, membership } : row)),
    );

    try {
      const saved = await firstValueFrom(this.users.change(customer.id, membership));
      this.customers.update((rows) => rows.map((row) => (row.id === saved.id ? saved : row)));
    } catch {
      this.customers.update((rows) => rows.map((row) => (row.id === customer.id ? customer : row)));
      this.notice.error('No se pudo actualizar la membresía');
    } finally {
      this.pendingId.set(null);
    }
  }
}
