import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { isRecord } from '../../shared/http/parse';
import { Customer, Membership } from './user.model';

@Service()
export class UserService {
  private readonly http = inject(HttpClient);

  change(id: number, membership: Membership): Observable<Customer> {
    return this.http
      .put<unknown>(`/api/admin/users/${id}`, { membership })
      .pipe(map((body) => parseCustomer(body)));
  }
}

export function parseCustomers(body: unknown): Customer[] {
  if (!Array.isArray(body)) {
    throw new Error('Invalid customers response');
  }

  return body.map((item) => parseCustomer(item));
}

export function parseCustomer(body: unknown): Customer {
  if (!isRecord(body) || typeof body['id'] !== 'number' || typeof body['name'] !== 'string') {
    throw new Error('Invalid customer response');
  }
  if (typeof body['email'] !== 'string' || !isMembership(body['membership'])) {
    throw new Error('Invalid customer response');
  }

  return {
    id: body['id'],
    name: body['name'],
    email: body['email'],
    membership: body['membership'],
  };
}

function isMembership(value: unknown): value is Membership {
  return value === 'MEMBER' || value === 'NON_MEMBER';
}
